package gov.nic.eap.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class MailSender implements Serializable {

	private static final long serialVersionUID = -2014331764259584526L;

	private Long id;

	private Long siteid;

	private Long mailid;

	private String mailsubject;

	private String mailbody;

	private String toemailids;

	private String ccemailids;

	private String bccemailids;

	private Long createdby;

	private LocalDateTime createddate;

	private String mailsource;

	private Integer processid;

	private LocalDateTime initiateddate;

	private Boolean isexecuted;

	private LocalDateTime executeddate;

	private String remarks;
}
