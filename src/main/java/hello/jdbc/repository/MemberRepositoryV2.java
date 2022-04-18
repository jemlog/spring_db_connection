package hello.jdbc.repository;

import hello.jdbc.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

/**
 * JDBC  - DataSource, JDBCUtils 사용
 */
@Slf4j
public class MemberRepositoryV2 {

    private final DataSource dataSource;

    public MemberRepositoryV2(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public Member save(Member member) throws SQLException {
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
            log.info("db error",e);
            throw e;
        }
        finally {
            // 외부 리소스를 사용 중
            // 커넥션 안 닫으면 연결 계속 유지 됨
            // 항상 리소스 정리하는 코드는 무조건 실행되는 finally에서 실행해야 한다.
            // 지금은 orm mapper나 jpa에서 리소스 정리 다 해준다.
        close(con,pstmt,null);
        }


    }

    // 옛날엔 이렇게 했다...
    public Member findById(Connection con, String memberId) throws SQLException {
        String sql = "select * from member where member_id = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

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
            log.error("db error",e);
            throw e;
        }
        finally {
            // 레포지토리에서는 connection을 끊어버리면 안된다.
            // 무조건 서비스 계층에서 닫아줘야 한다.
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(pstmt);
           // JdbcUtils.closeConnection(con);
        }
    }

    public Member findById(String memberId) throws SQLException {
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
            log.error("db error",e);
            throw e;
        }
        finally {
            close(con,pstmt,rs);
        }
    }


    public void update(Connection con, String memberId, int money) throws SQLException {
        String sql = "update member set money=? where member_id=?";


        PreparedStatement pstmt = null;

        try {

            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, memberId);
            int resultSize = pstmt.executeUpdate();
            log.info("resultSize={}", resultSize);
            // next 호출 시 데이터 있는지 체크
            // 첫번째 데이터 있으면

        } catch (SQLException e) {
            log.error("db error",e);
            throw e;
        }
        finally {
            close(con,pstmt,null);
        }
    }

    public void delete(String memberId) throws SQLException {
        String sql = "delete from member where member_id=?";
        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, memberId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            log.error("db error",e);
            throw e;
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
        JdbcUtils.closeConnection(con);
    }


    private Connection getConnection() throws SQLException {
        Connection con = dataSource.getConnection();
        log.info("get connection={}, class={}", con,con.getClass());
        return con;
    }
}
