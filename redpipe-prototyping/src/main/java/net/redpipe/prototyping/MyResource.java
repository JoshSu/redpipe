package net.redpipe.prototyping;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.Stream;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.reactivex.ext.web.client.HttpResponse;
import io.vertx.reactivex.ext.web.client.WebClient;
import net.redpipe.fibers.Fibers;

@Path("/hello")
public class MyResource {
	
	@GET
	public String hello(){
		return "hello bean";
	}

	@Path("2")
	@GET
	public Response hello2(){
		return Response.ok("hello 2").build();
	}
	
	@Path("3")
	@GET
	public void hello3(@Suspended final AsyncResponse asyncResponse,
		      // Inject the Vertx instance
		      @Context Vertx vertx){
		System.err.println("Creating client");
		HttpClientOptions options = new HttpClientOptions();
		options.setSsl(true);
		options.setTrustAll(true);
		options.setVerifyHost(false);
		HttpClient client = vertx.createHttpClient(options);
		client.getNow(443,
				"www.google.com", 
				"/robots.txt", 
				resp -> {
					System.err.println("Got response");
					resp.bodyHandler(body -> {
						System.err.println("Got body");
						asyncResponse.resume(Response.ok(body.toString()).build());
					});
				});
		System.err.println("Created client");
	}


	@Path("5")
	@GET
	public void hello5(@Suspended final AsyncResponse asyncResponse,
		      // Inject the Vertx instance
		      @Context Vertx vertx){
		io.vertx.reactivex.core.Vertx rxVertx = io.vertx.reactivex.core.Vertx.newInstance(vertx);
		System.err.println("Creating client");
		HttpClientOptions options = new HttpClientOptions();
		options.setSsl(true);
		options.setTrustAll(true);
		options.setVerifyHost(false);
		io.vertx.reactivex.core.http.HttpClient client = rxVertx.createHttpClient(options);
		// DOES NOT WORK: https://github.com/vert-x3/vertx-rx/issues/13
		Observable<io.vertx.reactivex.core.http.HttpClientResponse> responseHandler = client.get(443,
				"www.google.com", 
				"/robots.txt").toObservable();

		responseHandler.map(resp -> {
			System.err.println("Got response");
			return resp.toObservable(); 
		})
		.subscribe(body -> {
			System.err.println("Got body");
			asyncResponse.resume(Response.ok(body.toString()).build());
		});
		
		System.err.println("Created client");
	}

	@Path("6")
	@GET
	public void hello6(@Suspended final AsyncResponse asyncResponse,
		      // Inject the Vertx instance
		      @Context Vertx vertx){
		io.vertx.reactivex.core.Vertx rxVertx = io.vertx.reactivex.core.Vertx.newInstance(vertx);
		System.err.println("Creating client");
		WebClientOptions options = new WebClientOptions();
		options.setSsl(true);
		options.setTrustAll(true);
		options.setVerifyHost(false);
		WebClient client = WebClient.create(rxVertx, options);
		Single<HttpResponse<io.vertx.reactivex.core.buffer.Buffer>> responseHandler = client.get(443,
				"www.google.com", 
				"/robots.txt").rxSend();

		responseHandler
			.doAfterTerminate(() -> client.close())
			.subscribe(body -> {
			System.err.println("Got body");
			asyncResponse.resume(Response.ok(body.body().toString()).build());
		});
		
		System.err.println("Created client");
	}

	@Path("7")
	@GET
	public CompletionStage<String> hello7(@Context Vertx vertx){
		io.vertx.reactivex.core.Vertx rxVertx = io.vertx.reactivex.core.Vertx.newInstance(vertx);
		System.err.println("Creating client");
		WebClientOptions options = new WebClientOptions();
		options.setSsl(true);
		options.setTrustAll(true);
		options.setVerifyHost(false);
		WebClient client = WebClient.create(rxVertx, options);
		Single<HttpResponse<io.vertx.reactivex.core.buffer.Buffer>> responseHandler = client.get(443,
				"www.google.com", 
				"/robots.txt").rxSend();

		CompletableFuture<String> ret = new CompletableFuture<>();
		responseHandler
			.doAfterTerminate(() -> client.close())
			.subscribe(body -> {
			System.err.println("Got body");
			ret.complete(body.body().toString());
		});
		
		System.err.println("Created client");
		return ret;
	}

	@Path("7error")
	@GET
	public CompletionStage<String> hello7Error(@Context Vertx vertx){
		io.vertx.reactivex.core.Vertx rxVertx = io.vertx.reactivex.core.Vertx.newInstance(vertx);
		System.err.println("Creating client");
		WebClientOptions options = new WebClientOptions();
		options.setSsl(true);
		options.setTrustAll(true);
		options.setVerifyHost(false);
		WebClient client = WebClient.create(rxVertx, options);
		Single<HttpResponse<io.vertx.reactivex.core.buffer.Buffer>> responseHandler = client.get(443,
				"www.google.com", 
				"/robots.txt").rxSend();

		CompletableFuture<String> ret = new CompletableFuture<>();
		responseHandler
			.doAfterTerminate(() -> client.close())
			.subscribe(body -> {
			System.err.println("Got body");
			
			ret.completeExceptionally(new MyException());
		});
		System.err.println("Created client");
		return ret;
	}

	@Path("8")
	@GET
	public Single<String> hello8(@Context io.vertx.reactivex.core.Vertx rxVertx){
		System.err.println("Creating client");
		WebClientOptions options = new WebClientOptions();
		options.setSsl(true);
		options.setTrustAll(true);
		options.setVerifyHost(false);
		WebClient client = WebClient.create(rxVertx, options);
		Single<HttpResponse<io.vertx.reactivex.core.buffer.Buffer>> responseHandler = client.get(443,
				"www.google.com", 
				"/robots.txt").rxSend();

		System.err.println("Created client");
		return responseHandler.map(body -> {
			System.err.println("Got body");
			return body.body().toString();
		}).doAfterTerminate(() -> client.close());
	}

	@Path("8user")
	@Produces("text/json")
	@GET
	public Single<DataClass> hello8User(@Context io.vertx.reactivex.core.Vertx rxVertx){
		System.err.println("Creating client");
		WebClientOptions options = new WebClientOptions();
		options.setSsl(true);
		options.setTrustAll(true);
		options.setVerifyHost(false);
		WebClient client = WebClient.create(rxVertx, options);
		Single<HttpResponse<io.vertx.reactivex.core.buffer.Buffer>> responseHandler = client.get(443,
				"www.google.com", 
				"/robots.txt").rxSend();

		System.err.println("Created client");
		return responseHandler.map(body -> {
			System.err.println("Got body");
			return new DataClass(body.body().toString());
		}).doAfterTerminate(() -> client.close());
	}

	@Path("8error")
	@GET
	public Single<String> hello8Error(@Context io.vertx.reactivex.core.Vertx rxVertx){
		System.err.println("Creating client");
		WebClientOptions options = new WebClientOptions();
		options.setSsl(true);
		options.setTrustAll(true);
		options.setVerifyHost(false);
		WebClient client = WebClient.create(rxVertx, options);
		Single<HttpResponse<io.vertx.reactivex.core.buffer.Buffer>> responseHandler = client.get(443,
				"www.google.com", 
				"/robots.txt").rxSend();

		System.err.println("Created client");
		return responseHandler
				.doAfterTerminate(() -> client.close())
				.map(body -> {
			System.err.println("Got body");
			throw new MyException();
		});
	}

	@Path("9")
	@GET
	@Produces(MediaType.SERVER_SENT_EVENTS)
	public Observable<String> hello9(@Context io.vertx.reactivex.core.Vertx rxVertx){
		System.err.println("Creating timer");
		return rxVertx.periodicStream(1000).toObservable().map(r -> {
			System.err.println("Tick: "+r);
			return "Timer: "+System.currentTimeMillis();
		});
	}

	@Path("9nostream")
	@Produces("text/json")
	@GET
	public Observable<String> hello9nostream(@Context io.vertx.reactivex.core.Vertx rxVertx){
		System.err.println("Creating timer");
		return rxVertx.periodicStream(1000).toObservable().map(r -> {
			System.err.println("Tick: "+r);
			return "Timer: "+System.currentTimeMillis();
		}).take(3);
	}

	@Path("9chunked")
	@Produces("text/json")
	@GET
	@Stream
	public Observable<String> hello9chunked(@Context io.vertx.reactivex.core.Vertx rxVertx){
		System.err.println("Creating timer");
		return rxVertx.periodicStream(1000).toObservable().map(r -> {
			System.err.println("Tick: "+r);
			return "Timer: "+System.currentTimeMillis();
		});
	}

	@Path("9error")
	@GET
	public Observable<String> hello9Error(@Context io.vertx.reactivex.core.Vertx rxVertx){
		System.err.println("Creating timer");
		int[] i = new int[]{0};
		return rxVertx.periodicStream(1000).toObservable().map(r -> {
			System.err.println("Tick: "+r);
			if(i[0]++ < 5)
				return "Timer: "+System.currentTimeMillis();
			throw new MyException();
		});
	}

	@Produces("text/json")
	@Path("9user")
	@GET
	public Observable<DataClass> hello9User(@Context io.vertx.reactivex.core.Vertx rxVertx){
		System.err.println("Creating timer");
		return rxVertx.periodicStream(1000).toObservable().map(r -> {
			System.err.println("Tick: "+r);
			return new DataClass("Timer: "+System.currentTimeMillis());
		});
	}

	@Path("coroutines/1")
	@GET
	public Single<Response> helloAsync(@Context io.vertx.reactivex.core.Vertx rxVertx){
		return Fibers.fiber(() -> {
			System.err.println("Creating client");
			WebClientOptions options = new WebClientOptions();
			options.setSsl(true);
			options.setTrustAll(true);
			options.setVerifyHost(false);
			WebClient client = WebClient.create(rxVertx, options);
			Single<HttpResponse<io.vertx.reactivex.core.buffer.Buffer>> responseHandler = client.get(443,
					"www.google.com", 
					"/robots.txt").rxSend();

			System.err.println("Got response");

			HttpResponse<io.vertx.reactivex.core.buffer.Buffer> httpResponse = Fibers.await(responseHandler);
			System.err.println("Got body");
			client.close();
			
			return Response.ok(httpResponse.body().toString()).build();
		});
	}
}
