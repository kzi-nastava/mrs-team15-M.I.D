package rs.ac.uns.ftn.asd.ridenow.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;

@Controller
public class DeepLinkRedirectController {

    @GetMapping("/redirect/driver-activation/{token}")
    public void redirectToApp(@PathVariable String token, HttpServletResponse response) throws IOException {
        String appLink = "ridenow://driver-activation/" + token;

        String html = "<!DOCTYPE html>" +
                "<html><head>" +
                "<meta charset=\"UTF-8\">" +
                "<script>window.location.href = \"" + appLink + "\";</script>" +
                "</head><body>" +
                "<p>Opening RideNow app...</p>" +
                "<p>If nothing happens, <a href=\"" + appLink + "\">click here</a>.</p>" +
                "</body></html>";

        response.setContentType("text/html; charset=UTF-8");
        response.getWriter().write(html);
    }
}