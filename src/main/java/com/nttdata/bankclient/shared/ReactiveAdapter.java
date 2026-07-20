package com.nttdata.bankclient.shared;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public final class ReactiveAdapter {
    private ReactiveAdapter() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> Single<T> toSingle(Mono<T> mono) {
        return Single.fromPublisher(mono);
    }

    public static <T> Maybe<T> toMaybe(Mono<T> mono) {
        return Maybe.fromPublisher(mono);
    }

    public static <T> Flowable<T> toFlowable(Flux<T> flux) {
        return Flowable.fromPublisher(flux);
    }

    public static <T> Mono<T> toMono(Single<T> single) {
        return Mono.from(single.toFlowable());
    }

    public static <T> Mono<T> toMono(Maybe<T> maybe) {
        return Mono.from(maybe.toFlowable());
    }

    public static <T> Flux<T> toFlux(Flowable<T> flowable) {
        return Flux.from(flowable);
    }
}
