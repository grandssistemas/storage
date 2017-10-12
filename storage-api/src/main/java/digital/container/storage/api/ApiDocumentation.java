package digital.container.storage.api;


public final class ApiDocumentation {
    protected ApiDocumentation() {}

    public static final String POST_PERMISSION_CONTAINER = "Register a key to allow you to write data to storage.";

    public static final String PARAM_CONTAINER_KEY = "The key registered in the storage in the endpoint: /api/permission-container";
    public static final String PARAM_SHARED = "To leave the public record, to have access to it without a token.";
    public static final String PARAM_FILE = "The file that will be saved in storage.";
    public static final String PARAM_FILES = "The files that will be saved in storage(Maximum limit of 500 per request).";
    public static final String PARAM_TOKEN_SOFTWARE_HOUSE = "The software house token to share the registry with it.";
    public static final String PARAM_TOKEN_ACCOUNTANT = "The accountant token to share the registry with it.";

    public static final String POST_LOCAL_FILE_UPLOAD = "Upload anything that will be saved locally in storage.";
    public static final String GET_HASH_LOCAL_FILE = "View the file saved in storage by the hash.";
    public static final String GET_DOWNLOAD_LOCAL_FILE = "Download file.";
    public static final String GET_PUBLIC_HASH_LOCAL_FILE = "View the public file saved in storage by the hash.";
    public static final String DELETE_HASH_LOCAL_FILE = "Remove the file saved in storage by the hash.";
    public static final String DELETE_ID_LOCAL_FILE = "Remove the file saved in storage by id.";
}
