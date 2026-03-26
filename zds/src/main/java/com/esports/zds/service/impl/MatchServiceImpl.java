package com.esports.zds.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.esports.zds.entity.MatchApplication;
import com.esports.zds.entity.MatchInfo;
import com.esports.zds.repository.MatchApplicationRepository;
import com.esports.zds.repository.MatchInfoRepository;
import com.esports.zds.service.MatchService;

/**
 * 约赛业务实现类
 */
@Service
public class MatchServiceImpl implements MatchService {

    @Autowired
    private MatchInfoRepository matchInfoRepository;

    @Autowired
    private MatchApplicationRepository applicationRepository;

    @Override
    public MatchInfo publishMatch(MatchInfo matchInfo) {
        matchInfo.setStatus(0); // 待匹配
        return matchInfoRepository.save(matchInfo);
    }

    @Override
    public List<MatchInfo> listMatches(Integer status, String gameProject) {
        if (status != null && gameProject != null) {
            return matchInfoRepository.findAll().stream()
                    .filter(m -> m.getStatus().equals(status) && m.getGameProject().equals(gameProject))
                    .collect(Collectors.toList());
        }
        if (status != null) {
            return matchInfoRepository.findByStatus(status);
        }
        if (gameProject != null) {
            return matchInfoRepository.findByGameProject(gameProject);
        }
        return matchInfoRepository.findAll();
    }

    @Override
    public MatchInfo getById(Long id) {
        return matchInfoRepository.findById(id).orElse(null);
    }

    @Override
    public void applyMatch(Long matchId, Long applicantTeamId) {
        // 1. 检查是否已申请
        if (applicationRepository.existsByMatchIdAndApplicantTeamId(matchId, applicantTeamId)) {
            throw new RuntimeException("已提交过申请，请勿重复操作");
        }

        // 2. 检查约赛状态
        MatchInfo match = matchInfoRepository.findById(matchId).orElseThrow();
        if (match.getStatus() != 0) {
            throw new RuntimeException("该约赛已不在待匹配状态");
        }

        // 3. 创建申请
        MatchApplication app = new MatchApplication();
        app.setMatchId(matchId);
        app.setApplicantTeamId(applicantTeamId);
        app.setStatus(0); // 待审核
        applicationRepository.save(app);
    }

    @Override
    @Transactional
    public void reviewApplication(Long applicationId, Integer status) {
        MatchApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("申请不存在"));
        
        if (app.getStatus() != 0) {
            throw new RuntimeException("该申请已处理过");
        }

        app.setStatus(status);
        applicationRepository.save(app);

        // 如果通过申请，更新约赛信息
        if (status == 1) {
            MatchInfo match = matchInfoRepository.findById(app.getMatchId()).orElseThrow();
            match.setGuestTeamId(app.getApplicantTeamId());
            match.setStatus(1); // 已匹配
            matchInfoRepository.save(match);
            
            // 拒绝该约赛的其他所有待审核申请
            List<MatchApplication> others = applicationRepository.findByMatchId(match.getId());
            for (MatchApplication other : others) {
                if (other.getId().equals(applicationId)) continue;
                if (other.getStatus() == 0) {
                    other.setStatus(2); // 自动拒绝
                    applicationRepository.save(other);
                }
            }
        }
    }

    @Override
    public List<MatchApplication> listApplications(Long matchId) {
        return applicationRepository.findByMatchId(matchId);
    }

    @Override
    public List<MatchInfo> listMyMatches(Long teamId) {
        return matchInfoRepository.findAll().stream()
                .filter(m -> teamId.equals(m.getHostTeamId()) || teamId.equals(m.getGuestTeamId()))
                .collect(Collectors.toList());
    }
}
