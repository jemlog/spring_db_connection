package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 템플릿 // 템플릿 콜백 패턴 공부
 */
@Slf4j
public class MemberServiceV3_2 {

    // 데이터 소스를 직접 사용하는게 문제 ! 의존적이다!
  //  private final DataSource dataSource;
    // DataSourceTransactionManager는 JDBC와 관련된 트랜잭션 매니져 구현체!
  //  private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;
    private final TransactionTemplate txTemplate;

    public MemberServiceV3_2(MemberRepositoryV3 memberRepository, PlatformTransactionManager transactionManager) {
        this.memberRepository = memberRepository;
        // 트랜잭션 템플릿은 그냥 클래스이다. 유연성이 없다. 그러나 platformtransactionmanager로 주입받으면 인터페이스라서 유연성이 있다.
        this.txTemplate = new TransactionTemplate(transactionManager);
    }

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {

        /*
        이 코드 안에서 트랜잭션 시작 하고, 성공적이면 정상 커밋, 실패하면 예외 터짐
         */
        txTemplate.executeWithoutResult((status)->{
            // 비즈니스 로직
            try {
                bizLogic(fromId, toId, money);
            } catch (SQLException e) {
                throw new IllegalStateException(e);
            }
        });

    }

    private void bizLogic(String fromId, String toId, int money) throws SQLException {
        Member fromMember = memberRepository.findById(fromId);
        Member toMember = memberRepository.findById(toId);

        memberRepository.update(fromId, fromMember.getMoney() - money);
        validation(toMember);
        memberRepository.update(toId,toMember.getMoney() + money);
    }

    private void release(Connection con) {
        if(con != null)
        {
            try
            {
                // 바꾸고 돌려줘야 한다! -> 커넥션 풀 고려
                con.setAutoCommit(true);
                con.close();
            }
            catch (Exception e)
            {
                log.info("error",e);
            }
        }
    }

    private void validation(Member toMember) {
        if( toMember.getMemberId().equals("ex"))
        {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }
}
