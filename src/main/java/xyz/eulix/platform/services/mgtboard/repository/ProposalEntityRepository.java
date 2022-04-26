package xyz.eulix.platform.services.mgtboard.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.mgtboard.entity.ProposalEntity;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProposalEntityRepository implements PanacheRepository<ProposalEntity> {
    // 根据id更新资源
    private static final String UPDATE_BY_ID = "content=?1, email=?2, phone_number=?3, image_urls=?4, updated_at=now() where id=?5";

    // 根据user_domain更新资源
    private static final String UPDATE_BY_USER_DOMAIN = "user_domain=?1, updated_at=now() where user_domain=?2";

    public void updateById(Long proposalId, String content, String email, String phoneNumber, String imageUrls) {
        this.update(UPDATE_BY_ID, content, email, phoneNumber, imageUrls, proposalId);
    }

    public void updateUserDomainByUserDomain(String userDomain, String userDomainOld) {
        this.update(UPDATE_BY_USER_DOMAIN, userDomain, userDomainOld);
    }
}
