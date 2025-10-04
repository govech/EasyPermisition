package com.cairong.permission.exceptions

/**
 * 权限请求相关异常的基类
 */
abstract class PermissionException(message: String, cause: Throwable? = null) : Exception(message, cause)

/**
 * 权限请求超时异常
 */
class PermissionRequestTimeoutException(message: String = "权限请求超时") : PermissionException(message)

/**
 * 权限请求被取消异常
 */
class PermissionRequestCancelledException(message: String = "权限请求被取消") : PermissionException(message)

/**
 * 权限请求频率限制异常
 */
class PermissionRateLimitException(message: String = "权限请求被频率限制") : PermissionException(message)

/**
 * 权限请求配置异常
 */
class PermissionConfigurationException(message: String, cause: Throwable? = null) : PermissionException(message, cause)

/**
 * 权限请求状态异常
 */
class PermissionStateException(message: String) : PermissionException(message)