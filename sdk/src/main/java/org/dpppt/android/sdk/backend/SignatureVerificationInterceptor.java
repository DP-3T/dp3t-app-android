/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.dpppt.android.sdk.backend;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;

import org.dpppt.android.sdk.util.SignatureUtil;

import okhttp3.Interceptor;
import okhttp3.Response;

public class SignatureVerificationInterceptor implements Interceptor {

	private static final long PEEK_MEMORY_LIMIT = 64 * 1024 * 1024L;

	private final PublicKey publicKey;

	public SignatureVerificationInterceptor(@NonNull PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	@NonNull
	@Override
	public Response intercept(@NonNull Chain chain) throws IOException {
		Response response = chain.proceed(chain.request());

		if (!response.isSuccessful()) {
			return response;
		}

		String jwsHeader = response.headers().get(SignatureUtil.HTTP_HEADER_JWS);
		if (jwsHeader == null) {
			throw new SignatureException("JWS header not found");
		}

		if (publicKey == null) {
			throw new SignatureException("Public key not specified");
		}

		byte[] signedContentHash = SignatureUtil.getVerifiedContentHash(jwsHeader, publicKey);

		byte[] body = response.peekBody(PEEK_MEMORY_LIMIT).bytes();

		byte[] actualContentHash;
		try {
			MessageDigest digest = MessageDigest.getInstance(SignatureUtil.HASH_ALGO);
			actualContentHash = digest.digest(body);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}

		if (!Arrays.equals(actualContentHash, signedContentHash)) {
			throw new SignatureException("Signature mismatch");
		}

		return response;
	}

}
