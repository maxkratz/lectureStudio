package org.lecturestudio.presenter.api.presenter;

import org.lecturestudio.core.app.ApplicationContext;
import org.lecturestudio.core.presenter.Presenter;
import org.lecturestudio.presenter.api.config.PresenterConfiguration;
import org.lecturestudio.presenter.api.view.DLZSettingsView;

import org.lecturestudio.web.api.exception.MatrixUnauthorizedException;
import org.lecturestudio.web.api.model.DLZRoom;
import org.lecturestudio.web.api.service.DLZRoomService;

import javax.inject.Inject;
import java.util.List;

/**
 * @author Daniel Schröter
 * functional part of the dlz settings section
 */
public class DLZSettingsPresenter extends Presenter<DLZSettingsView> {

    @Inject
    DLZSettingsPresenter(ApplicationContext context, DLZSettingsView view) {
        super(context, view);
    }

    /**
     * method to initialize the functions
     */
    @Override
    public void initialize() {
        PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
        org.lecturestudio.web.api.filter.AuthorizationFilter.setToken(config.getDLZAccessToken());
        
        view.setDLZAccessToken(config.DLZAccessToken());
        if(config.getDLZAccessToken().length() > 5) {
            try {
                view.setRoom(config.dlzRoomProperty());
                view.setRooms(DLZSetRoomList());
            } catch (Exception e) {
                config.setDlzRoom(null);
                view.setRoom(null);
            }
        }
        view.setOnClose(this::close);
        view.setOnReset(this::reset);
        view.refreshaccesstoken(this::DLZSaveAccessToken);
    }

    private List<DLZRoom> DLZSetRoomList() {
        List<DLZRoom> DLZRooms = List.of();
        try {
            DLZRooms = DLZRoomService.getRooms();
        } catch (MatrixUnauthorizedException e) {
            PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
            config.setDlzRoom(null);
            handleException(e, "", e.getMessage());
        }
        return DLZRooms;
    }

    private void DLZSaveAccessToken(){
        PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
        String token = view.getDLZAccessTokenInField();
        config.setDLZAccessToken(token);
        org.lecturestudio.web.api.filter.AuthorizationFilter.setToken(config.getDLZAccessToken());
        view.setRooms(DLZSetRoomList());
    }

    private void reset() {
        PresenterConfiguration config = (PresenterConfiguration) context.getConfiguration();
    }

}