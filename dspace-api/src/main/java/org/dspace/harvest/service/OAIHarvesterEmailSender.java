/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */
package org.dspace.harvest.service;

import java.util.List;

import org.dspace.harvest.HarvestedCollection;

/**
 * Service to send an email related to the result of an harvesting process.
 *
 * @author Luca Giamminonni (luca.giamminonni at 4science.it)
 *
 */
public interface OAIHarvesterEmailSender {

    /**
     * Send an email to the given recipient address to notify that the harvesting
     * related to the given harvested collection completed with some errors.
     *
     * @param recipient           the mail's recipient
     * @param harvestedCollection the harvested collection
     * @param errors              the error messages
     */
    void notifyCompletionWithErrors(String recipient, HarvestedCollection harvestedCollection, List<String> errors);

    /**
     * Send an email to the given recipient address to notify that the harvesting
     * related to the given harvested collection fails for the given exception.
     *
     * @param recipient           the mail's recipient
     * @param harvestedCollection the harvested collection
     * @param ex                  the exception occured
     */
    void notifyFailure(String recipient, HarvestedCollection harvestedCollection, Exception ex);
}
