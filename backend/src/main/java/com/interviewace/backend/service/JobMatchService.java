package com.interviewace.backend.service;
import com.interviewace.backend.dto.JobMatchAnalysisDto;
import com.interviewace.backend.dto.JobMatchRequest;
import java.util.List;
public interface JobMatchService {
    JobMatchAnalysisDto analyzeJobMatch(Long resumeId, JobMatchRequest request);
    List<JobMatchAnalysisDto> getJobMatchHistory(Long resumeId);
    JobMatchAnalysisDto getLatestJobMatch(Long resumeId);
}
