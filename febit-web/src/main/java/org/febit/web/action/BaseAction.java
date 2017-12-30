package org.febit.web.action;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import org.febit.service.PageResult;
import org.febit.service.ServiceResult;
import org.febit.util.ArraysUtil;
import org.febit.vtor.Vtor;
import org.febit.web.ActionRequest;
import org.febit.web.WebApp;
import org.febit.web.meta.Action;
import org.febit.web.meta.ActionAnnotation;
import org.febit.web.meta.DELETE;
import org.febit.web.meta.GET;
import org.febit.web.meta.PATCH;
import org.febit.web.meta.POST;
import org.febit.web.render.JsonData;
import org.febit.web.render.JsonVtorData;
import org.febit.web.render.Redirect;
import org.febit.web.render.ResponeError;
import org.febit.web.util.JsonPageUtil;
import org.febit.web.util.RenderUtil;

/**
 *
 * @author zqq90
 */
public abstract class BaseAction {

  protected static final Object OK = ServiceResult.SUCCESS_RESULT;
  protected static ResponeError PAGE_404 = ResponeError.ERROR_404;

  @POST
  @Action("")
  @ActionAnnotation
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  public static @interface CREATE {
  }

  @GET
  @Action("$id")
  @ActionAnnotation
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  public static @interface SELECT {
  }

  @GET
  @Action("search")
  @ActionAnnotation
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  public static @interface SEARCH {
  }

  @PATCH
  @Action("$id")
  @ActionAnnotation
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  public static @interface UPDATE {
  }

  @DELETE
  @Action("$id")
  @ActionAnnotation
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  public static @interface REMOVE {
  }

  @DELETE
  @Action("$ids/multi")
  @ActionAnnotation
  @Retention(RetentionPolicy.RUNTIME)
  @Target({ElementType.METHOD})
  public static @interface REMOVE_MULTI {
  }

  protected static ActionRequest attr(String key, Object value) {
    return WebApp.request().attr(key, value);
  }

  protected static Redirect redirect(String path) {
    return new Redirect(path, null);
  }

  protected static Redirect redirect(String path, String msg) {
    return new Redirect(path, msg);
  }

  protected static Object _error(int error) {
    return ServiceResult.error(error);
  }

  protected static JsonData _json(Object value) {
    return new JsonData(value);
  }

  protected static Object _json(ServiceResult result) {
    return result;
  }

  protected static Object _json(ServiceResult result, String... profiles) {
    return RenderUtil.toJsonResult(result, profiles);
  }

  protected static JsonData _json(String[] keys, Object[] values) {
    return _json(ArraysUtil.asMap(keys, values));
  }

  protected static JsonData _json(Object value, String... profiles) {
    return new JsonData(value, profiles);
  }

  protected static JsonData _page(List list, String... profiles) {
    return new JsonData(JsonPageUtil.wrap(list), profiles);
  }

  protected static JsonData _page(Object[] list, String... profiles) {
    return new JsonData(JsonPageUtil.wrap(list), profiles);
  }

  protected static JsonData _json(PageResult pageResults) {
    return new JsonData(JsonPageUtil.wrap(pageResults));
  }

  protected static JsonData _json(PageResult pageResults, String... profiles) {
    return new JsonData(JsonPageUtil.wrap(pageResults), profiles);
  }

  protected static JsonData _json(String[] keys, Object[] values, String... profiles) {
    return _json(ArraysUtil.asMap(keys, values), profiles);
  }

  protected static Object _error(String error) {
    return ServiceResult.error(error);
  }

  protected static Object _error(String error, Object... args) {
    return ServiceResult.error(error, args);
  }

  protected static Object _error(int code, String error) {
    return ServiceResult.error(code, error);
  }

  protected static JsonVtorData _vtor(List<Vtor> vtors) {
    return new JsonVtorData(vtors, null);
  }

  protected static JsonVtorData _vtor(List<Vtor> vtors, String targetName) {
    return new JsonVtorData(vtors, targetName);
  }
}
