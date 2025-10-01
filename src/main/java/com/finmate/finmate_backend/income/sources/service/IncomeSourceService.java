package com.finmate.finmate_backend.income.sources.service;

import com.finmate.finmate_backend.income.sources.dto.IncomeSourceDtos.CreateIncomeSourceRequest;
import com.finmate.finmate_backend.income.sources.dto.IncomeSourceDtos.IncomeSourceItem;
import com.finmate.finmate_backend.income.sources.model.IncomeSource;
import com.finmate.finmate_backend.income.sources.repository.IncomeSourceRepository;
import com.finmate.finmate_backend.users.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IncomeSourceService {

    private final IncomeSourceRepository incomeSourceRepository;

    public List<IncomeSourceItem> getActive(User user) {
        return incomeSourceRepository.findActiveByUser(user)
                .stream()
                .map(this::toItem)
                .toList();
    }

    public IncomeSourceItem create(User user, CreateIncomeSourceRequest req) {
        if (req.name() == null || req.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (incomeSourceRepository.existsByUserAndNameIgnoreCaseAndDeletedFalse(user, req.name().trim())) {
            throw new IllegalArgumentException("Income source with this name already exists");
        }

        IncomeSource entity = new IncomeSource();
        entity.setUser(user);
        entity.setName(req.name().trim());
        entity.setColor(req.color());

        IncomeSource saved = incomeSourceRepository.save(entity);
        return toItem(saved);
    }

    private IncomeSourceItem toItem(IncomeSource s) {
        return new IncomeSourceItem(s.getId(), s.getName(), s.getColor());
    }
}


