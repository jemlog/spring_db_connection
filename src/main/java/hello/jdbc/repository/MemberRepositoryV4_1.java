package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.ex.MyDbException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * 예외 누수 문제 해결
 * 체크 예외 -> 런타임 예외
 * MemberRepository 인터페이스 사용
 * throws SQLException 제거
 */
@Slf4j
public class MemberRepositoryV4_1 implements MemberRepository{

    private final DataSource dataSource;

    public MemberRepositoryV4_1(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    @Override
    public Member save(Member member){
        String sql = "insert into member(member_id,money) values (?,?)"; // 오류 무시!

        Connection con  = null;

        PreparedStatement pstmt = null;

        try
        {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, member.getMemberId());
            pstmt.setInt(2,member.getMoney());
            pstmt.executeUpdate(); // 영향 받은 raw 수만큼 int 값 반환
            return member;
        }
        catch (SQLException e)
        {
            throw new MyDbException(e);
        }
        finally {
            // 외부 리소스를 사용 중
            // 커넥션 안 닫으면 연결 계속 유지 됨
            // 항상 리소스 정리하는 코드는 무조건 실행되는 finally에서 실행해야 한다.
            // 지금은 orm mapper나 jpa에서 리소스 정리 다 해준다.
        close(con,pstmt,null);
        }


    }

    @Override
    public Member findById(String memberId){
        String sql = "select * from member where member_id = ?";
        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1,memberId);
            rs = pstmt.executeQuery();
            // next 호출 시 데이터 있는지 체크
            // 첫번째 데이터 있으면
            if(rs.next())
            {
                Member member = new Member();
                member.setMemberId(rs.getString("member_id"));
                member.setMoney(rs.getInt("money"));
                return member;
            }
            else
            {
                throw new NoSuchElementException("member not found memberId= " + memberId);
            }
        } catch (SQLException e) {
            throw new MyDbException(e);
        }
        finally {
            close(con,pstmt,rs);
        }
    }

    @Override
    public void update(String memberId, int money){
        String sql = "update member set money=? where member_id=?";


        Connection con = null;
        PreparedStatement pstmt = null;

        try {

            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
            // next 호출 시 데이터 있는지 체크
            // 첫번째 데이터 있으면

        } catch (SQLException e) {
           throw new MyDbException(e);
        }
        finally {
            close(con,pstmt,null);
        }
    }


    @Override
    public void delete(String memberId){
        String sql = "delete from member where member_id=?";
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new MyDbException(e);
        }
        finally {

            JdbcUtils.closeStatement(pstmt);
          //  JdbcUtils.closeConnection(con);
        }

    }

    private void close(Connection con, Statement stmt, ResultSet rs)
    {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils를 사용해야 함
        DataSourceUtils.releaseConnection(con,dataSource);
    }


    private Connection getConnection() throws SQLException {
        // 주의! 트랜잭션 동기화를 사용하려면 DataSourceUtils 사용해야 함
        Connection con = DataSourceUtils.getConnection(dataSource);

        log.info("get connection={}, class={}", con,con.getClass());
        return con;
    }
}
