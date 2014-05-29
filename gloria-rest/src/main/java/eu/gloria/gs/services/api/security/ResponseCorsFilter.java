package eu.gloria.gs.services.api.security;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

public class ResponseCorsFilter implements ContainerResponseFilter {

	@Override
	public ContainerResponse filter(ContainerRequest req,
			ContainerResponse contResp) {

		//String method = req.getMethod();
		
		ResponseBuilder resp = Response.fromResponse(contResp.getResponse());
		
		resp.header("Access-Control-Allow-Origin", "*");
		resp.header("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, x-requested-with");
		resp.header("Access-Control-Allow-Credentials", "true");
		resp.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
		resp.header("Access-Control-Max-Age", "1209600");

		contResp.setResponse(resp.build());
		return contResp;
	}

}