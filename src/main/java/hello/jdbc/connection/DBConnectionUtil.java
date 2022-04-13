package hello.jdbc.connection;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static hello.jdbc.connection.ConnectionConst.*;

@Slf4j
public class DBConnectionUtil {

    public static Connection getConnection()
    {
        try
        {
            /*
            DriverManager는 JDBC가 찾아옴
            가져온 커넥션은 org.h2.jdbc.JdbcConnection 구현체이다.
            의문 : 어떻게 db에 맞춰서 가져오는거지?
             */
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            log.info("get connection={}, class={}",connection,connection.getClass());
            return connection;
        }
        catch (SQLException e)
        {
            // checked exception을 runtime exception으로 반환해서 던짐
            throw new IllegalStateException(e);
        }

    }
}
