package pt.ist.expenditureTrackingSystem.presentationTier.actions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import myorg.presentationTier.actions.ContextBaseAction;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import pt.ist.expenditureTrackingSystem.domain.File;
import pt.ist.expenditureTrackingSystem.domain.organization.Person;
import pt.ist.expenditureTrackingSystem.presentationTier.messageHandling.MessageHandler;
import pt.ist.expenditureTrackingSystem.presentationTier.messageHandling.MessageHandler.MessageType;
import pt.ist.expenditureTrackingSystem.presentationTier.util.FileUploadBean;
import pt.ist.fenixWebFramework.renderers.components.state.IViewState;
import pt.ist.fenixWebFramework.renderers.model.MetaObject;
import pt.ist.fenixWebFramework.renderers.utils.RenderUtils;
import pt.ist.fenixframework.DomainObject;
import pt.ist.fenixframework.pstm.Transaction;

public abstract class BaseAction extends ContextBaseAction {

    private MessageHandler messageHandler = null;

    @Override
    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
	    final HttpServletResponse response) throws Exception {
	messageHandler = new MessageHandler();
	request.setAttribute(MessageHandler.MESSAGE_HANDLER_NAME, messageHandler);
	return super.execute(mapping, form, request, response);

    }

    protected Person getLoggedPerson() {
	return Person.getLoggedPerson();
    }

    protected <T> T getAttribute(final HttpServletRequest request, final String attributeName) {
	final T t = (T) request.getAttribute(attributeName);
	return t == null ? (T) request.getParameter(attributeName) : t;
    }

    protected <T extends DomainObject> T getDomainObject(final HttpServletRequest request, final String attributeName) {
	final String parameter = request.getParameter(attributeName);
	final Long oid = parameter != null ? Long.valueOf(parameter) : (Long) request.getAttribute(attributeName);
	return oid == null ? null : (T) Transaction.getObjectForOID(oid.longValue());
    }

    protected <T extends Object> T getRenderedObject() {
	final IViewState viewState = RenderUtils.getViewState();
	return (T) getRenderedObject(viewState);
    }

    protected <T extends Object> T getRenderedObject(final String id) {
	final IViewState viewState = RenderUtils.getViewState(id);
	return (T) getRenderedObject(viewState);
    }

    protected <T extends Object> T getRenderedObject(final IViewState viewState) {
	if (viewState != null) {
	    MetaObject metaObject = viewState.getMetaObject();
	    if (metaObject != null) {
		return (T) metaObject.getObject();
	    }
	}
	return null;
    }

    protected byte[] consumeInputStream(final FileUploadBean fileUploadBean) {
	final InputStream inputStream = fileUploadBean.getInputStream();
	return consumeInputStream(inputStream);
    }

    protected ActionForward download(final HttpServletResponse response, final String filename, final byte[] bytes,
	    final String contentType) throws IOException {
	final OutputStream outputStream = response.getOutputStream();
	response.setContentType(contentType);
	response.setHeader("Content-disposition", "attachment; filename=" + filename.replace(" ", "_"));
	response.setContentLength(bytes.length);
	if (bytes != null) {
	    outputStream.write(bytes);
	}
	outputStream.flush();
	outputStream.close();
	return null;
    }

    protected ActionForward download(final HttpServletResponse response, final File file) throws IOException {
	String filename = file.getFilename();
	if (filename == null) {
	    filename = file.getDisplayName();
	}
	return file != null && file.getContent() != null ? download(response, filename != null ? filename : "", file.getContent()
		.getBytes(), file.getContentType()) : null;
    }

    public void addMessage(String key, String bundle, String... args) {
	messageHandler.saveMessage(bundle, key, MessageType.WARN, args);
    }

    public void addErrorMessage(String key, String bundle, String... args) {
	messageHandler.saveMessage(bundle, key, MessageType.ERROR, args);
    }

}
