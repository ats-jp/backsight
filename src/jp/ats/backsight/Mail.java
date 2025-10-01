package jp.ats.backsight;

import static jp.ats.substrate.U.isAvailable;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.URLDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import jp.ats.substrate.util.CP932;

public class Mail {

	private final MimeMessage message;

	private final List<InternetAddress> to = new LinkedList<InternetAddress>();

	private final List<InternetAddress> cc = new LinkedList<InternetAddress>();

	private final List<InternetAddress> bcc = new LinkedList<InternetAddress>();

	private String content;

	private DataSource attachment;

	private String attachmentName;

	private InternetAddress redirect;

	public Mail(String smtp, String username, String password) {
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", smtp);
		props.setProperty("mail.smtp.auth", "true");
		message = new MimeMessage(Session.getInstance(
			props,
			new SMTPAuthenticator(username, password)));
	}

	public Mail(String smtp) {
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", smtp);
		message = new MimeMessage(Session.getInstance(props));
	}

	public void addMailTo(String address) throws AddressException {
		addMailTo(new InternetAddress(address));
	}

	public void addCC(String address) throws AddressException {
		addCC(new InternetAddress(address));
	}

	public void addBCC(String address) throws AddressException {
		addBCC(new InternetAddress(address));
	}

	public void setFrom(String address) throws MessagingException {
		setFrom(new InternetAddress(address));
	}

	public void addMailTo(InternetAddress address) {
		to.add(selectAddress(address));
	}

	public void addCC(InternetAddress address) {
		cc.add(selectAddress(address));
	}

	public void addBCC(InternetAddress address) {
		bcc.add(selectAddress(address));
	}

	public void setFrom(InternetAddress address) throws MessagingException {
		message.setFrom(address);
	}

	public void setSubject(String subject) throws MessagingException {
		try {
			message.setSubject(MimeUtility.encodeText(
				CP932.treatForJIS(subject),
				"iso-2022-jp",
				"B"));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e.getMessage());
		}
	}

	public void setMessage(String message) {
		content = CP932.treatForJIS(message);
	}

	public void attach(File file) {
		attachment = new FileDataSource(file);
	}

	public void attach(URL url) {
		attachment = new URLDataSource(url);
	}

	public void attach(DataSource attachment) {
		this.attachment = attachment;
	}

	public void setAttachmentName(String attachmentName) {
		this.attachmentName = attachmentName;
	}

	public void send() throws MessagingException {
		InternetAddress[] tos = to.toArray(new InternetAddress[to.size()]);
		InternetAddress[] ccs = cc.toArray(new InternetAddress[cc.size()]);
		InternetAddress[] bccs = bcc.toArray(new InternetAddress[bcc.size()]);

		message.setRecipients(Message.RecipientType.TO, tos);
		if (ccs.length > 0) message.setRecipients(Message.RecipientType.CC, ccs);
		if (bccs.length > 0) message.setRecipients(
			Message.RecipientType.BCC,
			bccs);

		if (attachment == null) {
			setMessageOnly();
		} else {
			setMessageAndFile();
		}

		sendInternal(message);
	}

	protected void setRedirect(String redirect) throws AddressException {
		this.redirect = new InternetAddress(redirect);
	}

	protected void sendInternal(MimeMessage message) throws MessagingException {
		Transport.send(message);
	}

	private void setMessageOnly() throws MessagingException {
		message.setContent(content, "text/plain; charset=\"iso-2022-jp\"");
	}

	private void setMessageAndFile() throws MessagingException {
		MimeMultipart multi = new MimeMultipart();

		MimeBodyPart text = new MimeBodyPart();
		text.setContent(content, "text/plain; charset=\"iso-2022-jp\"");
		multi.addBodyPart(text);

		MimeBodyPart attach = new MimeBodyPart();
		DataHandler handler = new DataHandler(attachment);
		attach.setDataHandler(handler);
		attach.setFileName(!isAvailable(attachmentName)
			? attachment.getName()
			: attachmentName);
		multi.addBodyPart(attach);

		message.setContent(multi);
	}

	private InternetAddress selectAddress(InternetAddress address) {
		if (redirect != null) return redirect;
		return address;
	}

	private static class SMTPAuthenticator extends Authenticator {

		private final PasswordAuthentication auth;

		private SMTPAuthenticator(String username, String password) {
			auth = new PasswordAuthentication(username, password);
		}

		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return auth;
		}
	}
}
