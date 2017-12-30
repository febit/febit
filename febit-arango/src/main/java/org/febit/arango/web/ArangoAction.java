package org.febit.arango.web;

import org.febit.arango.ArangoSearchForm;
import org.febit.arango.ArangoService;
import org.febit.form.AddForm;
import org.febit.form.ModifyForm;
import org.febit.form.PageForm;
import org.febit.form.util.BaseFormUtil;
import org.febit.service.ServiceResult;
import org.febit.web.action.BaseAction;

/**
 *
 * @author zqq90
 */
public abstract class ArangoAction extends BaseAction {

    protected abstract ArangoService service();

    protected Object _delete(String id) {
        return _json(service().delete(id));
    }

    protected Object _deleteMulti(String[] ids) {
        if (ids == null) {
            return _error(ServiceResult.ERROR_PARAM_REQUIRED);
        }
        return _json(service().delete(ids));
    }

    protected Object _info(String id, Class type) {
        final ArangoService service = service();
        return _json(service.find(id, type));
    }

    protected Object _add(final AddForm form) {
        final ArangoService service = service();
        final int profile = _getFormProfile();
        if (!form.valid(profile, true)) {
            return _vtor(form.getVtors());
        }
        return service.add(form, profile);
    }

    protected Object _modify(final ModifyForm form) {
        final ArangoService service = service();
        final int profile = _getFormProfile();
        if (!form.valid(profile, false)) {
            return _vtor(form.getVtors());
        }
        return service.modify(form, profile);
    }

    public Object _page(ArangoSearchForm form, PageForm pageForm, Class type) {
        return _json(service().page(form, pageForm, type));
    }

    protected int _getFormProfile() {
        return BaseFormUtil.getFormProfile(this.getClass());
    }
}
