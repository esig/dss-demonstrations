package eu.europa.esig.dss.web.controller;

import eu.europa.esig.dss.asic.common.merge.ASiCContainerMerger;
import eu.europa.esig.dss.asic.common.merge.DefaultContainerMerger;
import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.web.WebAppUtils;
import eu.europa.esig.dss.web.model.ContainerMergeForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@Controller
@SessionAttributes(value = { "containerMergeForm", "mergedContainer" })
@RequestMapping(value = "/merge-containers")
public class ContainerMergeController {

    private static final Logger LOG = LoggerFactory.getLogger(ContainerMergeController.class);

    private static final String CONTAINERS_MERGE = "containers-merge";

    private static final String[] ALLOWED_FIELDS = { "documentsToMerge" };

    @InitBinder
    public void setAllowedFields(WebDataBinder webDataBinder) {
        webDataBinder.setAllowedFields(ALLOWED_FIELDS);
    }

    @RequestMapping(method = RequestMethod.GET)
    public String display(Model model, HttpServletRequest request) {
        ContainerMergeForm containerMergeForm = new ContainerMergeForm();
        model.addAttribute("containerMergeForm", containerMergeForm);
        return CONTAINERS_MERGE;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String mergeContainers(Model model, HttpServletResponse response,
                                  @ModelAttribute("containerMergeForm") @Valid ContainerMergeForm containerMergeForm, BindingResult result) {
        if (result.hasErrors()) {
            if (LOG.isDebugEnabled()) {
                List<ObjectError> allErrors = result.getAllErrors();
                for (ObjectError error : allErrors) {
                    LOG.debug(error.getDefaultMessage());
                }
            }
            return CONTAINERS_MERGE;
        }

        List<DSSDocument> containers = WebAppUtils.toDSSDocuments(containerMergeForm.getDocumentsToMerge());
        ASiCContainerMerger merger = DefaultContainerMerger.fromDocuments(containers.toArray(new DSSDocument[0]));
        DSSDocument mergedContainer = merger.merge();

        MimeType mimeType = mergedContainer.getMimeType();
        if (mimeType != null) {
            response.setContentType(mimeType.getMimeTypeString());
        }
        response.setHeader("Content-Transfer-Encoding", "binary");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + mergedContainer.getName() + "\"");
        try (InputStream is = mergedContainer.openStream(); OutputStream os = response.getOutputStream()) {
            Utils.copy(is, os);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return null;
    }

}
