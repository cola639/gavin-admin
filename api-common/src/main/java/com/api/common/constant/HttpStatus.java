package com.api.common.constant;

/**
 * HTTP status codes used for REST API responses.
 * Provides semantic constants for success, client errors, server errors, and warnings.
 *
 * @author
 */
public class HttpStatus
{
    /**
     * Operation succeeded
     */
    public static final int SUCCESS = 200;

    /**
     * Resource created successfully
     */
    public static final int CREATED = 201;

    /**
     * Request has been accepted for processing
     */
    public static final int ACCEPTED = 202;

    /**
     * Operation executed successfully but no content returned
     */
    public static final int NO_CONTENT = 204;

    /**
     * Resource has been permanently moved
     */
    public static final int MOVED_PERM = 301;

    /**
     * Redirect
     */
    public static final int SEE_OTHER = 303;

    /**
     * Resource has not been modified
     */
    public static final int NOT_MODIFIED = 304;

    /**
     * Bad request - missing or invalid parameters
     */
    public static final int BAD_REQUEST = 400;

    /**
     * Unauthorized - authentication required
     */
    public static final int UNAUTHORIZED = 401;

    /**
     * Forbidden - access denied or authorization expired
     */
    public static final int FORBIDDEN = 403;

    /**
     * Resource or service not found
     */
    public static final int NOT_FOUND = 404;

    /**
     * HTTP method not allowed
     */
    public static final int BAD_METHOD = 405;

    /**
     * Resource conflict or resource locked
     */
    public static final int CONFLICT = 409;

    /**
     * Unsupported media type or data format
     */
    public static final int UNSUPPORTED_TYPE = 415;

    /**
     * Internal server error
     */
    public static final int ERROR = 500;

    /**
     * Not implemented - feature or API not available
     */
    public static final int NOT_IMPLEMENTED = 501;

    /**
     * System warning message
     */
    public static final int WARN = 601;
}
