package com.zmcsoft.rex.oauth2.server;

import lombok.AllArgsConstructor;
import org.hswebframework.web.authorization.basic.web.AuthorizedToken;
import org.hswebframework.web.authorization.basic.web.ParsedToken;
import org.hswebframework.web.authorization.basic.web.UserTokenParser;
import org.hswebframework.web.authorization.oauth2.server.OAuth2AccessToken;
import org.hswebframework.web.authorization.oauth2.server.exception.GrantTokenException;
import org.hswebframework.web.authorization.oauth2.server.token.AccessTokenService;
import org.hswebframework.web.authorization.token.UserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.hswebframework.web.oauth2.core.ErrorType;
import org.hswebframework.web.oauth2.core.OAuth2Constants;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author zhouhao
 * @since 1.0
 */
@AllArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class OAuth2UserTokenParser implements UserTokenParser {

    private AccessTokenService accessTokenService;

    private UserTokenManager userTokenManager;

    public static final String tokenType = "oauth2_access_token";

    @Override
    public ParsedToken parseToken(HttpServletRequest request) {
        if (request.getRequestURI().contains("oauth2") && request.getParameter(OAuth2Constants.grant_type) != null) {
            return null;
        }

        String accessToken = request.getParameter("access_token");
        if (StringUtils.isEmpty(accessToken)) {
            String authHeader = request.getHeader(OAuth2Constants.authorization);
            if (authHeader == null) {
                return null;
            }
            if (authHeader.contains(" ")) {
                String[] autzInfo = authHeader.split("[ ]");
                if (autzInfo.length > 1) {
                    accessToken = autzInfo[1];
                } else {
                    return null;
                }
            } else {
                accessToken = authHeader;
            }
        }
        String finalToken = accessToken;

        UserToken token = userTokenManager.getByToken(accessToken);

        if (token != null) {
            if (!token.isEffective()) {
                userTokenManager.signOutByToken(token.getToken());
                throw new GrantTokenException(ErrorType.EXPIRED_TOKEN);
            }
            return new ParsedToken() {
                @Override
                public String getToken() {
                    return finalToken;
                }

                @Override
                public String getType() {
                    return tokenType;
                }
            };
        }

        OAuth2AccessToken auth2AccessToken = accessTokenService.getTokenByAccessToken(accessToken);
        if (null != auth2AccessToken) {
            Long time = auth2AccessToken.getUpdateTime() != null ? auth2AccessToken.getUpdateTime() : auth2AccessToken.getCreateTime();
            if (System.currentTimeMillis() - time > auth2AccessToken.getExpiresIn() * 1000L) {
                throw new GrantTokenException(ErrorType.EXPIRED_TOKEN);
            }
            return new AuthorizedToken() {
                @Override
                public String getUserId() {
                    return auth2AccessToken.getOwnerId();
                }

                @Override
                public String getToken() {
                    return finalToken;
                }

                @Override
                public String getType() {
                    return tokenType;
                }

                @Override
                public long getMaxInactiveInterval() {
                    return (auth2AccessToken.getExpiresIn() * 1000);
                }
            };
        }
        throw new GrantTokenException(ErrorType.EXPIRED_REFRESH_TOKEN);
    }
}
