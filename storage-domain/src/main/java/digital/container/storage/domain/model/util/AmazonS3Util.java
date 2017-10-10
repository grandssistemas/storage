package digital.container.storage.domain.model.util;


public class AmazonS3Util {

    protected AmazonS3Util() {}
    public static final String ACCESS_KEY_ID = System.getProperty("amazon.s3.access_key_id");
    public static final String SECRET_ACCESS_KEY = System.getProperty("amazon.s3.secret_access_key");
    public static final String ANYTHING_BUCKET = System.getProperty("amazon.s3.anything_bucket");
    public static final String TAX_DOCUMENT_BUCKET = System.getProperty("amazon.s3.tax_document_bucket");
}
