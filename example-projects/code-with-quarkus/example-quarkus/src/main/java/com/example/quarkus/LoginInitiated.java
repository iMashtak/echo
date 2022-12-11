package com.example.quarkus;

import io.github.imashtak.echo.core.*;

import static com.example.quarkus.LoginInitiated.*;

public class LoginInitiated
    extends Task<LoginFailed, LoginSucceed>
    implements SelfHandler {

    private final String login;
    private final String password;

    public LoginInitiated(String login, String password) {
        super(LoginFailed.class, LoginSucceed.class);
        this.login = login;
        this.password = password;
    }


    @Override
    public void handleSelf(Bus bus) {
        if (login.equals("admin") && password.equals("admin")) {
            bus.publish(new LoginSucceed(this, "token"));
        }
        throw new RuntimeException("User not found");
    }

    @Override
    public void onException(Bus bus, Throwable ex) {
        bus.publish(new LoginFailed(this, ex));
    }

    public static class LoginSucceed extends Success {

        private final String token;

        protected LoginSucceed(Task<?, ?> task, String token) {
            super(task);
            this.token = token;
        }

        public String getToken() {
            return token;
        }
    }

    public static class LoginFailed extends Failure {

        protected LoginFailed(Task<?, ?> task, Throwable cause) {
            super(task, cause);
        }
    }
}
