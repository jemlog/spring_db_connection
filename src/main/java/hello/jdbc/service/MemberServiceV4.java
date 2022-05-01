package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



/**
 * 예외 누수 문제 해결
 * SQLException 해결
 * MemberService 인터페이스 의존
 */
@Slf4j
public class MemberServiceV4 {

    // 데이터 소스를 직접 사용하는게 문제 ! 의존적이다!
  //  private final DataSource dataSource;
    // DataSourceTransactionManager는 JDBC와 관련된 트랜잭션 매니져 구현체!
  //  private final PlatformTransactionManager transactionManager;
    private final MemberRepository memberRepository;
    public MemberServiceV4(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        // 트랜잭션 템플릿은 그냥 클래스이다. 유연성이 없다. 그러나 platformtransactionmanager로 주입받으면 인터페이스라서 유연성이 있다.

    }
    @Transactional
    public void accountTransfer(String fromId, String toId, int money){
                bizLogic(fromId, toId, money);
        }

    private void bizLogic(String fromId, String toId, int money){
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
