package eu.gloria.gs.services.api.data.dbservices;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface UserDataService {

	public void create();

	public List<UserEntry> get(@Param(value = "name_") String name);

	public List<UserEntry> getActive(@Param(value = "name_") String name);

	public UserEntry getByToken(@Param(value = "token_") String token);

	public void save(UserEntry entry);

	public void setActive(@Param(value = "token_") String token);

	public void setInactive(@Param(value = "token_") String token);

	public void setOthersInactive(@Param(value = "name_") String name, @Param(value = "token_") String token);

	public boolean containsUser(@Param(value = "name_") String name);

	public boolean containsToken(@Param(value = "token_") String token);

	public String getPassword(@Param(value = "name_") String name);

	public void setPassword(@Param(value = "name_") String name,
			@Param(value = "password_") String password);

	public void remove(@Param(value = "name_") String name);

	public Date getTokenUpdateDate(@Param(value = "token_") String token);

	public void setTokenUpdateDate(@Param(value = "token_") String token,
			@Param(value = "date_") Date tokenCreationDate);
}
