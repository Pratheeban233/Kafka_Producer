package gov.nic.eap.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsSender implements Serializable {

	private static final long serialVersionUID = 4985250302928896325L;

	private Long id;

	private Long siteid;

	private Long smsid;

	private String smsbody;

	private String mobilenos;

	private String smsresponseid;

	private Long createdby;

	private LocalDateTime createddate;

	private String smssource;

	private Integer processid;

	private LocalDateTime initiateddate;

	private Boolean isexecuted;

	private LocalDateTime executeddate;

	private String remarks;
}
