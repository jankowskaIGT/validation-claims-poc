
package com.scms.validation_claims.controller;

import com.scms.validation_claims.dto.CheckRequest;
import com.scms.validation_claims.dto.CheckResponse;
import com.scms.validation_claims.service.CheckClaimService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

/**
 * Widok WIN/CHECK – formularz do sprawdzenia biletu.
 * Używamy klasycznego name=... w HTML zamiast th:field, żeby uniknąć 500 przy parsowaniu szablonu.
 */
@Controller
@RequiredArgsConstructor
public class WinController {

    private final CheckClaimService service;

    /** Ping techniczny – szybki test, czy kontroler jest skanowany. */
    @GetMapping("/win/ping")
    @ResponseBody
    public String winPing() {
        return "WIN-OK";
    }

    /** GET: pokaż formularz (opcjonalny prefill z query params) */
    @GetMapping("/win")
    public String winForm(Model model,
                          @RequestParam(value = "customer_id", required = false) String customerId,
                          @RequestParam(value = "game_id", required = false) String gameId) {

        // (Opcjonalnie) przekaż obiekt do prefill w widoku
        CheckRequest req = new CheckRequest();
        if (customerId != null && !customerId.isBlank()) {
            req.setCustomer_id(customerId);
        }
        if (gameId != null && !gameId.isBlank()) {
            req.setGame_id(gameId);
        }
        model.addAttribute("checkRequest", req);

        return "win"; // => szuka templates/win.html
    }

    /** POST: sprawdzenie biletu i pokazanie wyniku */
    @PostMapping("/win/check")
    public String doCheck(@ModelAttribute("checkRequest") @Valid CheckRequest checkRequest,
                          BindingResult bindingResult,
                          Model model) {
        // Jeśli walidacja pól wejściowych nie przeszła, zostawiamy formularz
        if (bindingResult.hasErrors()) {
            return "win";
        }

        // Serwis skorzysta z game_id, aby w tle pobrać hash_algoritham z tabeli 'games'
        CheckResponse response = service.check(checkRequest);
        model.addAttribute("result", response);
        return "win";
    }
}
