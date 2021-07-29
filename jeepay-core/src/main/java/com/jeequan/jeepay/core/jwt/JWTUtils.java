/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.core.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/*
* JWT工具包
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 16:32
*/
public class JWTUtils {

    /** 生成token **/
    public static String generateToken(JWTPayload jwtPayload, String jwtSecret) {
        return Jwts.builder()
                .setClaims(jwtPayload.toMap())
                //过期时间 = 当前时间 + （设置过期时间[单位 ：s ] ）  token放置redis 过期时间无意义
                //.setExpiration(new Date(System.currentTimeMillis() + (jwtExpiration * 1000) ))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /** 根据token与秘钥 解析token并转换为 JWTPayload **/
    public static JWTPayload parseToken(String token, String secret){
        try {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();

            JWTPayload result = new JWTPayload();
            result.setSysUserId(claims.get("sysUserId", Long.class));
            result.setCreated(claims.get("created", Long.class));
            result.setCacheKey(claims.get("cacheKey", String.class));
            return result;


        } catch (Exception e) {
            return null; //解析失败
        }
    }


}
