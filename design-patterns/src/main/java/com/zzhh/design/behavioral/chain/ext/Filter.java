package com.zzhh.design.behavioral.chain.ext;

public interface Filter {

    void doFilter(Request request,Response response,FilterChain chain);
}
