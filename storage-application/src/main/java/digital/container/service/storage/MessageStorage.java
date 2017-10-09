package digital.container.service.storage;

public final class MessageStorage {

    public static final String FILE_NOT_FOUND = "No file found with this hash.";
    public static final String YOU_ALREADY_HAVE_ACCESS_TO_THE_CONTAINER = "You already have access to container.";
    public static final String TAX_DOCUMENT_WITH_THIS_ACCESS_KEY_ALREADY_EXISTS = "A tax document with this access key already exists.";
    public static final String CNPJ_OF_XML_IS_DIFFERENT_CONTAINER_KEY = "The CNPJ of XML is different from the key of the container informed.";
    public static final String WE_DONT_SUPPORT_TEMPLATE_REPORTED_IN_YOUR_XML = "We do not support the template reported in your xml.";
    public static final String FILE_ISNT_CANCELLATION_EVENT_TAX_DOCUMENT = "This file is not a cancellation event for a tax document.";
    public static final String CANCEL_EVENT_ALREADY_EXISTIS_THIS_ACCESS_KEY = "A cancel event already exists with this access key.";
    public static final String THERE_ISNT_TAX_DOCUMENT_THIS_ACCESS_KEY = "There is no tax document with this access key.";

    protected MessageStorage() {}
}
