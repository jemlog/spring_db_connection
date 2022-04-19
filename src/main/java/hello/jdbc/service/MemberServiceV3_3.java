package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - @Transactional AOP
 */
@Slf4j
public class MemberServiceV3_3 {

    // 데이터 소스를 직접 사용하는게 문제 ! 의존적이다!
  //  private final DataSource dataSource;
    // DataSourceTransactionManager는 JDBC와 관련된 트랜잭션 매니져 구현체!
  //  private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;
    public MemberServiceV3_3(MemberRepositoryV3 memberRepository) {
        this.memberRepository = memberRepository;
        // 트랜잭션 템플릿은 그냥 클래스이다. 유연성이 없다. 그러나 platformtransactionmanager로 주입받으면 인터페이스라서 유연성이 있다.

    }
    @Transactional
    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
                bizLogic(fromId, toId, money);
        }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId,toMember.getMoney() + money);
    }

    private void validation(Member toMember) {
        if( toMember.getMemberId().equals("ex"))
        {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
