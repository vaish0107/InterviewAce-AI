package com.interviewace.backend.service.impl;

import com.interviewace.backend.dto.*;
import com.interviewace.backend.entity.*;
import com.interviewace.backend.exception.*;
import com.interviewace.backend.repository.*;
import com.interviewace.backend.service.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.file.*;
import java.util.*;

@Service
public class JobMatchServiceImpl implements JobMatchService {
    private final ResumeRepository resumes;
    private final JobMatchAnalysisRepository matches;
    private final AuthenticatedUserService authenticatedUsers;
    private final AiResumeClient aiClient;
    private final JsonListMapper lists;
    private final JobMatchJsonMapper categories;
    public JobMatchServiceImpl(ResumeRepository resumes, JobMatchAnalysisRepository matches,
            AuthenticatedUserService authenticatedUsers, AiResumeClient aiClient,
            JsonListMapper lists, JobMatchJsonMapper categories) {
        this.resumes = resumes; this.matches = matches; this.authenticatedUsers = authenticatedUsers;
        this.aiClient = aiClient; this.lists = lists; this.categories = categories;
    }
    @Override
    public JobMatchAnalysisDto analyzeJobMatch(Long resumeId, JobMatchRequest request) {
        Resume resume = ownedResume(resumeId);
        String jobDescription = validateDescription(request);
        Path path = existingPath(resume.getFilePath());
        AiJobMatchResponse response = aiClient.matchJob(path, jobDescription);
        validateResponse(response);
        Map<String, JobMatchCategoryDto> categoryDtos = toCategories(response.categoryMatches());
        JobMatchAnalysis entity = new JobMatchAnalysis(resume, jobDescription);
        entity.setMatchPercentage(response.matchPercentage()); entity.setResumeSkillCount(response.resumeSkillCount());
        entity.setJobSkillCount(response.jobSkillCount()); entity.setMatchedSkills(lists.serialize(response.matchedSkills()));
        entity.setMissingSkills(lists.serialize(response.missingSkills()));
        entity.setAdditionalResumeSkills(lists.serialize(response.additionalResumeSkills()));
        entity.setCategoryMatches(categories.serialize(categoryDtos)); entity.setStrengths(lists.serialize(response.strengths()));
        entity.setRecommendations(lists.serialize(response.recommendations())); entity.setMatchingNote(response.matchingNote());
        return toDto(matches.saveAndFlush(entity));
    }
    @Override @Transactional(readOnly = true)
    public List<JobMatchAnalysisDto> getJobMatchHistory(Long resumeId) {
        Resume resume = ownedResume(resumeId);
        return matches.findByResumeIdOrderByCreatedAtDesc(resume.getId()).stream().map(this::toDto).toList();
    }
    @Override @Transactional(readOnly = true)
    public JobMatchAnalysisDto getLatestJobMatch(Long resumeId) {
        Resume resume = ownedResume(resumeId);
        return matches.findTopByResumeIdOrderByCreatedAtDesc(resume.getId()).map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Job match analysis not found"));
    }
    private Resume ownedResume(Long id) {
        Long userId = authenticatedUsers.getAuthenticatedUser().getId();
        return resumes.findByIdAndUserId(id, userId).orElseThrow(() -> new ResourceNotFoundException("Resume not found"));
    }
    private String validateDescription(JobMatchRequest request) {
        String value = request == null ? null : request.jobDescription();
        if (value == null || value.isBlank()) throw new InvalidResumeException("Job description is required");
        String trimmed = value.trim();
        if (trimmed.length() < 20) throw new InvalidResumeException("Job description must contain at least 20 characters");
        return trimmed;
    }
    private Path existingPath(String storedPath) {
        try {
            Path path = storedPath == null ? null : Path.of(storedPath).toAbsolutePath().normalize();
            if (path == null || !Files.isRegularFile(path)) throw new AiAnalysisException("Stored resume file is unavailable");
            return path;
        } catch (InvalidPathException exception) { throw new AiAnalysisException("Stored resume file is unavailable"); }
    }
    private void validateResponse(AiJobMatchResponse value) {
        if (value == null || value.matchPercentage() == null || value.matchPercentage() < 0 || value.matchPercentage() > 100)
            throw new AiAnalysisException("AI service returned an invalid match percentage");
        if (value.resumeSkillCount() == null || value.resumeSkillCount() < 0 || value.jobSkillCount() == null || value.jobSkillCount() < 0)
            throw new AiAnalysisException("AI service returned invalid skill counts");
        if (value.matchedSkills() == null || value.missingSkills() == null || value.additionalResumeSkills() == null
                || value.categoryMatches() == null || value.strengths() == null || value.recommendations() == null
                || value.matchingNote() == null)
            throw new AiAnalysisException("AI service returned an invalid job match response");
    }
    private Map<String, JobMatchCategoryDto> toCategories(Map<String, AiCategoryMatchDto> source) {
        Map<String, JobMatchCategoryDto> result = new LinkedHashMap<>();
        source.forEach((name, value) -> {
            if (name == null || value == null || value.requiredSkills() == null || value.matchedSkills() == null
                    || value.missingSkills() == null || value.matchPercentage() == null
                    || value.matchPercentage() < 0 || value.matchPercentage() > 100)
                throw new AiAnalysisException("AI service returned malformed category match data");
            result.put(name, new JobMatchCategoryDto(value.requiredSkills(), value.matchedSkills(),
                    value.missingSkills(), value.matchPercentage()));
        });
        return result;
    }
    private JobMatchAnalysisDto toDto(JobMatchAnalysis value) {
        return new JobMatchAnalysisDto(value.getId(), value.getResume().getId(), value.getMatchPercentage(),
                value.getResumeSkillCount(), value.getJobSkillCount(), lists.deserialize(value.getMatchedSkills()),
                lists.deserialize(value.getMissingSkills()), lists.deserialize(value.getAdditionalResumeSkills()),
                categories.deserialize(value.getCategoryMatches()), lists.deserialize(value.getStrengths()),
                lists.deserialize(value.getRecommendations()), value.getMatchingNote(), value.getCreatedAt());
    }
}
