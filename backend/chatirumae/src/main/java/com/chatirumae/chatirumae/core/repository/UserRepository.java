package com.chatirumae.chatirumae.core.repository;
import com.chatirumae.chatirumae.core.model.User;

public interface UserRepository {
    public User findById(String id);

    public User findByPortalId(String portalId);

    public void save(User user);

    public void update(User user);

    public void deleteById(String id);
}
