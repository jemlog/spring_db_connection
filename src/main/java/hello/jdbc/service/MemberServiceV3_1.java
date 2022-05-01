package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import hello.jdbc.repository.MemberRepositoryV3;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 트랜잭션 - 트랜잭션 매니저
 */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV3_1 {

    // 데이터 소스를 직접 사용하는게 문제 ! 의존적이다!
  //  private final DataSource dataSource;
    // DataSourceTransactionManager는 JDBC와 관련된 트랜잭션 매니져 구현체!
    private final PlatformTransactionManager transactionManager;
    private final MemberRepositoryV3 memberRepository;

    public void accountTransfer(String fromId,String toId, int money) throws SQLException {

        //트랜잭션 시작
        // 트랜잭션 매니저를 만들때, dataSource가 필요하고, transaction 시작할때는 설정값 넣어주면 된다.
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());



        try
        {

            // 비즈니스 로직 실행
            bizLogic(fromId, toId, money);
            // 트랜잭션 매니저의 핵심은 , 모든 구현체에 대해서 트랜잭션 시작 커밋 롤백 과정을 똑같이 해주는 것이다!
            transactionManager.commit(status);
        }
        catch (Exception e)
        {
              transactionManager.rollback(status);
              throw new IllegalStateException(e);
        }
         // 트랜잭션 매니져는 커밋하거나 롤백할때 알아서 커넥션 정리한다 즉 릴리즈 한다!


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
