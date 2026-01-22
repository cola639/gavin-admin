package com.api.system.imports.user;

import com.api.common.domain.SysUser;
import com.api.common.utils.SecurityUtils;
import com.api.common.utils.excel.ExcelHeaderMismatchException;
import com.api.common.utils.excel.ExcelReader;
import com.api.common.utils.excel.ExcelRow;
import com.api.common.utils.excel.ExcelSheet;
import com.api.framework.exception.ServiceException;
import com.api.system.imports.base.ImportBatchContext;
import com.api.system.imports.base.ImportRowGenerator;
import com.api.system.imports.base.ImportRowMapper;
import com.api.system.imports.base.ImportRowValidator;
import com.api.system.imports.base.ImportRowWriter;
import com.api.system.imports.base.ImportValidationResult;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Orchestrates user imports using the Excel pipeline. */
@Service
@RequiredArgsConstructor
public class UserImportService {

  private static final int IMPORT_BATCH_SIZE = 200;

  private final ExcelReader excelReader;
  private final ImportRowMapper<UserImportRow> rowMapper;
  private final ImportRowValidator<UserImportRow, UserImportContext> validator;
  private final ImportRowGenerator<UserImportRow, UserImportContext, SysUser> generator;
  private final ImportRowWriter<SysUser> writer;

  public UserImportResult importUsers(
      InputStream inputStream, boolean updateSupport, boolean dryRun) {
    if (inputStream == null) {
      throw new ServiceException("Excel input stream is required");
    }

    ExcelSheet sheet;
    try (InputStream in = inputStream) {
      sheet = excelReader.read(in, UserImportContract.readSpec());
    } catch (ExcelHeaderMismatchException e) {
      throw new ServiceException(
          "Excel headers do not match expected template. Expected: "
              + e.getExpected()
              + ", actual: "
              + e.getActual());
    } catch (Exception e) {
      throw new ServiceException("Failed to read Excel file: " + e.getMessage());
    }

    String operator = SecurityUtils.getUsername();
    ImportBatchContext batchContext = new ImportBatchContext(updateSupport);
    List<UserImportRowError> rowErrors = new ArrayList<>();
    List<SysUser> pendingSaves = new ArrayList<>(IMPORT_BATCH_SIZE);

    int createdCount = 0;
    int updatedCount = 0;

    for (ExcelRow excelRow : sheet.getRows()) {
      UserImportRow row = rowMapper.map(excelRow);
      ImportValidationResult<UserImportContext> validation = validator.validate(row, batchContext);

      if (!validation.isValid()) {
        rowErrors.add(
            new UserImportRowError(excelRow.getRowIndex(), row.getUserName(), validation.getErrors()));
        continue;
      }

      UserImportContext context = validation.getContext();
      boolean isUpdate = context != null && context.getExistingUser() != null;
      SysUser entity = generator.generate(row, context, operator);
      if (!dryRun) {
        pendingSaves.add(entity);
        if (pendingSaves.size() >= IMPORT_BATCH_SIZE) {
          writer.saveAll(pendingSaves);
          pendingSaves.clear();
        }
      }

      if (isUpdate) {
        updatedCount++;
      } else {
        createdCount++;
      }
    }

    if (!dryRun && !pendingSaves.isEmpty()) {
      writer.saveAll(pendingSaves);
      pendingSaves.clear();
    }

    int totalRows = sheet.getRows().size();
    int successCount = createdCount + updatedCount;
    int errorCount = rowErrors.size();

    return new UserImportResult(
        totalRows, successCount, createdCount, updatedCount, errorCount, rowErrors);
  }
}
