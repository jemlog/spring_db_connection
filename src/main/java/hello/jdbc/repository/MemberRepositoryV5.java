package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * SQLException Translator 추가
 */
@Slf4j
public class MemberRepositoryV5 implements MemberRepository{

    private final JdbcTemplate template;
    public MemberRepositoryV5(DataSource dataSource)
    {
        this.template = new JdbcTemplate(dataSource);
        // 어떤 db 쓰는지 정보를 찾아서 쓰기 때문에 dataSource를 필요로 한다.

    }

    @Override
    public Member save(Member member){
        String sql = "insert into member(member_id,money) values (?,?)"; // 오류 무시!
        template.update(sql, member.getMemberId(), member.getMoney());
        return member;


    }

    @Override
    public Member findById(String memberId){
        String sql = "select * from member where member_id = ?";
        Member member = template.queryForObject(sql, memberRowMapper(), memberId);
        return member;
    }

    private RowMapper<Member> memberRowMapper()
    {
        return (rs, rowNum) -> {
            Member member = new Member();
            member.setMemberId(rs.getString("member_id"));
            member.setMoney(rs.getInt("money"));
            return member;
        };
    }

    @Override
    public void update(String memberId, int money){
        String sql = "update member set money=? where member_id=?";
        template.update(sql,money,memberId);


    }


    @Override
    public void delete(String memberId){
        String sql = "delete from member where member_id=?";
        template.update(sql,memberId);

    }

}
