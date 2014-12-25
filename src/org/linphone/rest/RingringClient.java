package org.linphone.rest;


import org.androidannotations.annotations.rest.Accept;
import org.androidannotations.annotations.rest.Get;
import org.androidannotations.annotations.rest.Post;
import org.androidannotations.annotations.rest.Put;
import org.androidannotations.api.rest.MediaType;
import org.linphone.core.StatusResult;
import org.linphone.core.User;
import org.linphone.core.UserResult;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

import org.androidannotations.annotations.rest.Rest;
import org.springframework.web.client.RestTemplate;

@Rest(rootUrl = "https://api.ringring.io/v2", converters = { GsonHttpMessageConverter.class} )
public interface RingringClient {

    RestTemplate getRestTemplate();

    @Post("/user")
    @Accept(MediaType.APPLICATION_JSON)
    UserResult register(User user);

    @Get("/user/{email}")
    @Accept(MediaType.APPLICATION_JSON)
    UserResult getRingringUser(String email);

    @Get("/user/{email}/renewactivationcode")
    @Accept(MediaType.APPLICATION_JSON)
    StatusResult renewActivationCode(String email);

    @Put("/user/{email}")
    @Accept(MediaType.APPLICATION_JSON)
    StatusResult activate(User user, String email);
}
