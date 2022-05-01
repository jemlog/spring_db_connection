package hello.jdbc.exception;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class UncheckedTest {


    @Test
    void unchecked_catch()
    {
        Service service = new Service();
        service.callCatch();
    }
    /**
     * RuntimeException을 상속받은 예외는 언체크 예외가 됨
     */
    static class MyUncheckedException extends RuntimeException
    {
        public MyUncheckedException(String message) {
            super(message);
        }
    }

    static class Repository{
        public void call()
        {
            throw new MyUncheckedException("ex");
        }
    }

    static class Service
    {
        Repository repository = new Repository();
        public void callCatch()
        {
            try {
                repository.call();
            }
            catch (MyUncheckedException e)
            {
                log.info("myunchecked exception",e.getMessage(),e);
            }

        }


    }
}
