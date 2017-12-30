package org.febit.leancloud.web;

import org.febit.form.AddForm;
import org.febit.form.ModifyForm;
import org.febit.form.PageForm;
import org.febit.form.util.BaseFormUtil;
import org.febit.leancloud.Entity;
import org.febit.leancloud.service.LcSearchForm;
import org.febit.leancloud.service.LcService;
import org.febit.web.action.BaseAction;

/**
 *
 * @author Zqq
 */
public abstract class LcAction extends BaseAction {

  protected abstract LcService service();

  protected Object _delete(String id) {
    return _json(service().delete(id));
  }

//    protected Object _deleteMulti(long[] ids) {
//        if (ids == null) {
//            return _error(ServiceResult.REQ_ERR_EMPTY);
//        }
//        return _json(service().delete(ids));
//    }
  protected Object _info(String id) {
    final LcService service = service();
    return _json(service.find(id));
  }

  protected <T extends Entity> Object _info(String id, Class<T> destType) {
    final LcService service = service();
    return _json(service.find(id, destType));
  }

  protected Object _add(final AddForm form) {
    final LcService service = service();
    final int profile = _getFormProfile();
    if (!form.valid(profile, true)) {
      return _vtor(form.getVtors());
    }
    return service.add(form, profile);
  }

  protected Object _modify(final ModifyForm form) {
    final LcService service = service();
    final int profile = _getFormProfile();
    if (!form.valid(profile, false)) {
      return _vtor(form.getVtors());
    }
    return service.modify(form, profile);
  }

  public <T extends Entity> Object _page(LcSearchForm form, PageForm pageForm, Class<T> destType) {
    return _json(service().page(form, pageForm, destType));
  }

  public Object _page(LcSearchForm form, PageForm pageForm) {
    return _json(service().page(form, pageForm));
  }

  protected int _getFormProfile() {
    return BaseFormUtil.getFormProfile(this.getClass());
  }

}
