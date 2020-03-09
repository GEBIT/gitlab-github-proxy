package com.dkaedv.glghproxy.converter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.User;
import org.gitlab.api.models.GitlabCommit;
import org.gitlab.api.models.GitlabMergeRequest;
import org.gitlab.api.models.GitlabUser;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.env.MockEnvironment;

import com.dkaedv.glghproxy.Constants;

public class GitlabToGithubConverterTest {

	private static final MockEnvironment env = new MockEnvironment();
	
	@BeforeClass
	public static void initEnvironment() {
		env.setProperty(Constants.KEY_FALLBACK_AVATAR_URL, "https://my.dummy.avatarurl/");
	}
	
	@Test
	public void shouldConvertPullRequest() {
		GitlabMergeRequest mergeRequest = new GitlabMergeRequest();
		GitlabUser user = new GitlabUser();
		user.setEmail("hanswurscht@test.com");
		user.setId(5);
		mergeRequest.setAssignee(user);
		mergeRequest.setAuthor(user);
		mergeRequest.setId(15);
		mergeRequest.setIid(3);
		mergeRequest.setState("merged");
		
		PullRequest pull = GitlabToGithubConverter.convertMergeRequest(mergeRequest, "http://gitlab", "testns", "test", env);
		
		assertEquals("hanswurscht@test.com", pull.getAssignee().getEmail());
		assertEquals("http://gitlab/testns/test/merge_requests/3", pull.getHtmlUrl());
		
	}

	@Test
	public void shouldConvertMergedPullRequestWithNullAssignee() {
		GitlabMergeRequest mergeRequest = new GitlabMergeRequest();
		GitlabUser user = new GitlabUser();
		user.setEmail("hanswurscht@test.com");
		user.setUsername("hanswurscht");
		user.setId(5);
		mergeRequest.setAuthor(user);
		mergeRequest.setId(15);
		mergeRequest.setIid(3);
		mergeRequest.setState("merged");
		
		PullRequest pull = GitlabToGithubConverter.convertMergeRequest(mergeRequest, "http://gitlab", "testns", "test", env);
		
		assertEquals("hanswurscht@test.com", pull.getMergedBy().getEmail());
		
	}

	@Test
	public void shouldConvertEmptyCommitToEmptyFileList() {
		GitlabCommit commit = new GitlabCommit();
		GitlabUser user = new GitlabUser();
		user.setEmail("hanswurscht@test.com");
		user.setUsername("hanswurscht");
		user.setId(5);
		RepositoryCommit ghCommit = GitlabToGithubConverter.convertCommit(commit, Collections.emptyList(), user, env);
		assertNotNull(ghCommit.getFiles());
		assertEquals(0, ghCommit.getFiles().size());
	}
	
	@Test
	public void shouldProvideAvatarUrl() {
		GitlabUser user = new GitlabUser();
		user.setEmail("hanswurscht@test.com");
		user.setUsername("hanswurscht");
		user.setId(5);
		User ghUser = GitlabToGithubConverter.convertUser(user, env);
		String fallbackUrl = env.getProperty(Constants.KEY_FALLBACK_AVATAR_URL);
		assertNotNull(fallbackUrl);
		assertEquals(fallbackUrl, ghUser.getAvatarUrl());
		
		user.setAvatarUrl("https://my.custom.avatarurl");
		ghUser = GitlabToGithubConverter.convertUser(user, env);
		assertEquals("https://my.custom.avatarurl", ghUser.getAvatarUrl());
	}
}
