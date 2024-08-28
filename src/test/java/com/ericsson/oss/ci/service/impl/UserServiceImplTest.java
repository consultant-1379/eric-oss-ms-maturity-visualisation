/*******************************************************************************
 * COPYRIGHT Ericsson 2021
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.oss.ci.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.ericsson.oss.ci.domain.entity.UserEntity;
import com.ericsson.oss.ci.domain.repository.UserRepository;
import com.ericsson.oss.ci.service.UserService;

public class UserServiceImplTest {
	
	private UserRepository userRepoMock = mock(UserRepository.class);
    private UserService userService = new UserServiceImpl(userRepoMock);
	
	@Test
    void saveUser_Successful() {
        UserEntity userEntity = UserEntity.builder().userId("testUser").build();
        userService.saveUser(userEntity);
        verify(userRepoMock, times(1)).save(userEntity);
    }
	
    @Test
    void userCount() {
        long expectedCount = 0;
        when(userRepoMock.count()).thenReturn(expectedCount);
        long result = userService.userCount();
        assertEquals(expectedCount, result);
    }
    
    @Test
    void countDistinctUsersUntilToday() {
        Object[] user1 = {"user1", 3L};
        Object[] user2 = {"user2", 5L};
        List<Object[]> expectedResult = Arrays.asList(user1, user2);

        when(userRepoMock.countDistinctUsersByDayUntilDate(any(LocalDate.class))).thenReturn(expectedResult);

        List<Object[]> result = userService.countDistinctUsersUntilToday();
        assertEquals(expectedResult, result);
    }


}
