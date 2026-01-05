
package com.scms.validationclaims.controller;

import com.scms.validationclaims.model.Game;
import com.scms.validationclaims.model.Winner;
import com.scms.validationclaims.model.ClaimLog;
import com.scms.validationclaims.repository.GameRepository;
import com.scms.validationclaims.repository.WinnerRepository;
import com.scms.validationclaims.repository.ClaimLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ui")
@RequiredArgsConstructor
public class UiDataController {

    private final GameRepository gameRepo;
    private final WinnerRepository winnerRepo;
    private final ClaimLogRepository logRepo;

    @GetMapping("/games")
    public List<Game> games() {
        return gameRepo.findAll();
    }

    @GetMapping("/winners")
    public List<Winner> winners(@RequestParam(defaultValue = "50") int limit) {
        // limit clamp 1..500
        int size = Math.max(1, Math.min(limit, 500));
        return winnerRepo.findAll(PageRequest.of(0, size)).getContent();
    }

    @GetMapping("/claimlog")
    public List<ClaimLog> claimlog(@RequestParam(defaultValue = "50") int limit) {
        int size = Math.max(1, Math.min(limit, 500));
        return logRepo.findAll(PageRequest.of(0, size)).getContent();
    }
}
