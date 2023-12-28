package eu.europa.esig.dss.web.config;

import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

public class MultipartResolverProvider {

    /**
     * Defines the maximum file upload size
     */
    private long maxFileSize = -1;

    /**
     * Defines the maximum inMemory file size
     */
    private long maxInMemorySize = -1;

    /**
     * Defines whether the file load should be resolved lazily
     */
    private boolean resolveLazily = false;

    /**
     * Singleton instance
     */
    private static MultipartResolverProvider singleton;

    /**
     * Default constructor
     */
    private MultipartResolverProvider() {
        // empty
    }

    /**
     * Gets the current {@code MultipartResolverProvider} instance
     *
     * @return {@link MultipartResolverProvider}
     */
    public static MultipartResolverProvider getInstance() {
        if (singleton == null) {
            singleton = new MultipartResolverProvider();
        }
        return singleton;
    }

    /**
     * Gets max upload file size
     *
     * @return max upload file size
     */
    public long getMaxFileSize() {
        return maxFileSize;
    }

    /**
     * Sets maximum upload file size
     *
     * @param maxFileSize maximum file size
     */
    public void setMaxFileSize(long maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    /**
     * Gets max in memory size
     *
     * @return max in memory size
     */
    public long getMaxInMemorySize() {
        return maxInMemorySize;
    }

    /**
     * Sets maximum inMemory file size
     *
     * @param maxInMemorySize maximum in memory file size
     */
    public void setMaxInMemorySize(long maxInMemorySize) {
        this.maxInMemorySize = maxInMemorySize;
    }

    /**
     * Whether the file load should be resolved lazily
     *
     * @return TRUE if the file load should be resolved lazily, FALSE otherwise
     */
    public boolean isResolveLazily() {
        return resolveLazily;
    }

    /**
     * Sets whether the file load should be resolved lazily
     *
     * @param resolveLazily whether the file load should be resolved lazily
     */
    public void setResolveLazily(boolean resolveLazily) {
        this.resolveLazily = resolveLazily;
    }

    /**
     * Creates a new multipart resolver
     *
     * @return {@link MultipartResolver}
     */
    public MultipartResolver createMultipartResolver() {
        StandardServletMultipartResolver multipartResolver = new StandardServletMultipartResolver();
        multipartResolver.setResolveLazily(resolveLazily);
        return multipartResolver;
    }

}
