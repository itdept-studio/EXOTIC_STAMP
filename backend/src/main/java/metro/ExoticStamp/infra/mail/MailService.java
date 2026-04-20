package metro.ExoticStamp.infra.mail;

import metro.ExoticStamp.infra.mail.queue.MailQueueService;
import metro.ExoticStamp.infra.mail.template.EmailVerificationOtpTemplate;
import metro.ExoticStamp.infra.mail.template.OtpEmailTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final MailQueueService mailQueueService;
    private final MailProperties mailProperties;

    public void sendOtpEmail(String toEmail, String otp) {
        String body = OtpEmailTemplate.build(otp, mailProperties.getLogoUrl());
        mailQueueService.enqueueHtmlMail(toEmail, MailSubjectConstants.OTP_VERIFICATION_CODE, body);
    }

    public void sendEmailVerificationOtp(String toEmail, String otp) {
        String body = EmailVerificationOtpTemplate.build(otp, mailProperties.getLogoUrl());
        mailQueueService.enqueueHtmlMail(toEmail, MailSubjectConstants.EMAIL_VERIFICATION_CODE, body);
    }
}
