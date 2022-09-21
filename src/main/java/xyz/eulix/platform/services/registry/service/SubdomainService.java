package xyz.eulix.platform.services.registry.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.config.ApplicationProperties;
import xyz.eulix.platform.services.mgtboard.repository.ProposalEntityRepository;
import xyz.eulix.platform.services.mgtboard.repository.QaFeedbackEntityRepository;
import xyz.eulix.platform.services.registry.dto.registry.SubdomainStateEnum;
import xyz.eulix.platform.services.registry.entity.SubdomainEntity;
import xyz.eulix.platform.services.registry.repository.SubdomainEntityRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.OffsetDateTime;

@ApplicationScoped
public class SubdomainService {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    ApplicationProperties properties;

    @Inject
    SubdomainEntityRepository subdomainEntityRepository;

    @Inject
    ProposalEntityRepository proposalEntityRepository;

    @Inject
    QaFeedbackEntityRepository feedbackEntityRepository;

    @Transactional
    public void updateSubdomain(String boxUUID, String userId, String subdomain, String userDomain, String subdomainOld) {
        // subdomain
        subdomainEntityRepository.updateStateByBoxUUIDAndUserId(boxUUID, userId, SubdomainStateEnum.HISTORY.getState());
        SubdomainEntity subdomainEntity = new SubdomainEntity();
        {
            subdomainEntity.setBoxUUID(boxUUID);
            subdomainEntity.setUserId(userId);
            subdomainEntity.setSubdomain(subdomain);
            subdomainEntity.setUserDomain(userDomain);
            subdomainEntity.setState(SubdomainStateEnum.USED.getState());
        }
        subdomainEntityRepository.persist(subdomainEntity);
        String userDomainOld = subdomainOld + "." + properties.getRegistrySubdomain();
        // proposal
        proposalEntityRepository.updateUserDomainByUserDomain(userDomain, userDomainOld);
        // questionnaire_feedback
        feedbackEntityRepository.updateUserDomainByUserDomain(userDomain, userDomainOld);
    }
}
