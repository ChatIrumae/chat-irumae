package com.chatirumae.chatirumae.core.service;

import com.chatirumae.chatirumae.core.interfaces.UosApi;
import com.chatirumae.chatirumae.core.interfaces.UosSessionManager;
import com.chatirumae.chatirumae.core.model.UosSession;
import com.chatirumae.chatirumae.core.model.User;
import com.chatirumae.chatirumae.core.model.UserBasicInfo;
import com.chatirumae.chatirumae.core.repository.SessionRepository;
import com.chatirumae.chatirumae.core.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final UosSessionManager uosPortalSessionManager;
    private final UosApi uosPortalApi;

    public AuthService(UserRepository userRepository, SessionRepository sessionRepository,
                       UosSessionManager uosPortalSessionManager, UosApi uosPortalApi) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.uosPortalSessionManager = uosPortalSessionManager;
        this.uosPortalApi = uosPortalApi;
    }

    public String login(String portalId, String portalPassword) {
        // Null check
        if (portalId == null || portalPassword == null) {
            throw new IllegalArgumentException("Portal ID or password is null.");
        }

        // 먼저 DB에 사용자가 있는지 확인한다.
        User user = userRepository.findByPortalId(portalId);

        // 만약 사용자가 있는 경우
        if (user != null) {
            // 비밀번호가 맞는다면 세션을 생성하고 세션 키를 반환한다.
            if (user.checkPassword(portalPassword)) {
                String session = sessionRepository.createSession(user.getId());
                return session;
            }
            // 비밀번호가 틀린 경우 예외를 발생시킨다.
            else {
                throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
            }
        }

        // 사용자가 없는 경우 포털 로그인을 시도하여 로그인 정보가 올바른지 확인한다.
        else {
            System.out.println("hh");
            System.out.println(portalId);
            System.out.println(portalPassword);
            UosSession uosSession; // 변수를 try 블록 밖에서도 사용할 수 있도록 미리 선언합니다.

            try {
                // 이 부분에서 발생하는 모든 예외를 잡아서 처리합니다.
                uosSession = uosPortalSessionManager.createUosSession(portalId, portalPassword);
                System.out.println("session created: " + uosSession);
        
            } catch (Exception e) {
                // 💡 실패 로그를 자세히 출력합니다.
                System.err.println("## 포털 세션 생성 중 심각한 오류 발생 ##");
                e.printStackTrace(); // 예외의 전체 스택 트레이스(원인, 위치)를 출력합니다 (가장 중요!).
        
                // 오류가 발생했음을 알리고 흐름을 중단시키기 위해 새로운 예외를 던집니다.
                // 원래 발생한 예외(e)를 포함시켜 원인을 잃어버리지 않도록 합니다.
                throw new RuntimeException("포털 세션 생성에 실패했습니다. 로그를 확인하세요.", e);
            }

            // 만약 로그인 정보가 올바르다면 사용자를 생성하고 세션을 저장한 후 세션 키를 반환한다.
            if (uosSession != null) {
                System.out.println(22);
                // 포털에서 사용자 생성에 필요한 정보를 가져온다.
                UserBasicInfo userInfo = uosPortalApi.getUserBasicInfo(uosSession);
                String id = UUID.randomUUID().toString();

                // 새로운 사용자를 생성한다.
                user = new User(id, userInfo.getName(), userInfo.getStudentId(), 1, 1, portalId, "", "");
                user.setPassword(portalPassword);

                // 사용자를 DB에 저장한다.
                userRepository.save(user);

                // 세션을 생성하고 세션 키를 반환한다.
                String session = sessionRepository.createSession(user.getId());
                return session;
            }

            // 로그인 정보가 올바르지 않은 경우 예외를 발생시킨다.
            else {
                throw new IllegalArgumentException("포탈 로그인 정보가 올바르지 않습니다.");
            }
        }
    }

    public void logout(String session) {
        sessionRepository.deleteSession(session);
    }
}