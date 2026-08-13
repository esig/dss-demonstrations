package eu.europa.esig.dss.web.config;

import eu.europa.esig.dss.model.job.ValidationInfoRecord;
import eu.europa.esig.dss.model.tsl.LOTLInfo;
import eu.europa.esig.dss.model.tsl.TLInfo;
import eu.europa.esig.dss.model.tsl.TLParsingInfoRecord;
import eu.europa.esig.dss.validation.job.sync.SynchronizationStrategy;

import java.util.Date;

public class ExpirationAndSignatureCheckStrategyFixed implements SynchronizationStrategy<TLInfo, LOTLInfo> {

    /**
     * Define if expired trusted lists (next update after current time) are
     * supported
     */
    private boolean acceptExpiredTrustedList = false;

    /**
     * Define if trusted lists with invalid or indeterminate signatures are
     * supported
     */
    private boolean acceptInvalidTrustedList = false;

    /**
     * Define if expired list of trusted lists (next update after current time) are
     * supported
     */
    private boolean acceptExpiredListOfTrustedLists = false;

    /**
     * Define if list of trusted lists with invalid or indeterminate signatures are
     * supported
     */
    private boolean acceptInvalidListOfTrustedLists = false;

    /**
     * Default constructor instantiating object with null values
     */
    public ExpirationAndSignatureCheckStrategyFixed() {
        // empty
    }

    /**
     * Sets if expired trusted lists are supported (next update after current time)
     *
     * @param acceptExpiredTrustedList
     *                                 true/false if expired trusted lists can be
     *                                 synchronized
     */
    public void setAcceptExpiredTrustedList(boolean acceptExpiredTrustedList) {
        this.acceptExpiredTrustedList = acceptExpiredTrustedList;
    }

    /**
     * Sets if invalid trusted lists are supported (signature with FAILED or
     * INDETERMINATE Indication)
     *
     * @param acceptInvalidTrustedList
     *                                 true/false if invalid trusted lists can be
     *                                 synchronized
     */
    public void setAcceptInvalidTrustedList(boolean acceptInvalidTrustedList) {
        this.acceptInvalidTrustedList = acceptInvalidTrustedList;
    }

    /**
     * Sets if expired list of trusted lists and their TLs are supported (next
     * update after current time)
     *
     * @param acceptExpiredListOfTrustedLists
     *                                        true/false if expired list of trusted
     *                                        lists can be synchronized
     */
    public void setAcceptExpiredListOfTrustedLists(boolean acceptExpiredListOfTrustedLists) {
        this.acceptExpiredListOfTrustedLists = acceptExpiredListOfTrustedLists;
    }

    /**
     * Sets if invalid list of trusted lists and their TLs are supported (signature
     * with FAILED or INDETERMINATE Indication)
     *
     * @param acceptInvalidListOfTrustedLists
     *                                        true/false if invalid list of trusted
     *                                        lists can be synchronized
     */
    public void setAcceptInvalidListOfTrustedLists(boolean acceptInvalidListOfTrustedLists) {
        this.acceptInvalidListOfTrustedLists = acceptInvalidListOfTrustedLists;
    }

    @Override
    public boolean canBeSynchronized(TLInfo trustedList) {
        return isSyncSupported(trustedList, acceptExpiredTrustedList, acceptInvalidTrustedList);
    }

    @Override
    public boolean canBeSynchronized(LOTLInfo listOfTrustedList) {
        return isSyncSupported(listOfTrustedList, acceptExpiredListOfTrustedLists, acceptInvalidListOfTrustedLists);
    }

    private boolean isSyncSupported(TLInfo tlInfo, boolean syncExpired, boolean syncInvalid) {
        if (!syncExpired) {
            TLParsingInfoRecord parsingCacheInfo = tlInfo.getParsingCacheInfo();
            if (parsingCacheInfo == null || !parsingCacheInfo.isResultExist()) {
                return false;
            }
            Date currentDate = new Date();
            Date nextUpdateDate = parsingCacheInfo.getNextUpdateDate();
            if (nextUpdateDate == null || currentDate.after(nextUpdateDate)) {
                return false;
            }
        }

        if (!syncInvalid) {
            ValidationInfoRecord validationCacheInfo = tlInfo.getValidationCacheInfo();
            if (validationCacheInfo == null || !validationCacheInfo.isResultExist()) {
                return false;
            }
            return validationCacheInfo.isValid();
        }

        return true;
    }

}
