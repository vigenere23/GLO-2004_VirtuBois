package presentation.presenters;

import domain.controllers.LarmanController;
import domain.dtos.BundleDto;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import presentation.controllers.MainController;
import java.util.ArrayList;
import java.util.List;

import static helpers.ColorHelper.hex2Rgb;

public class ElevationViewPresenter3D implements IPresenter {

    private SubScene scene;
    private Camera camera;
    private Group group;
    private double anchorX, anchorY;
    private double anchorAngleX = 0;
    private double anchorAngleY = 0;
    private final DoubleProperty angleX = new SimpleDoubleProperty(0);
    private final DoubleProperty angleY = new SimpleDoubleProperty(0);
    private LarmanController larmanController;
    private MainController mainController;
    private List<BundleDto> allBundles;
    private Point2D lastPoint;
    private Point2D groupTranslate;
    private Point2D initGroupTranslate;


    public ElevationViewPresenter3D(StackPane parent, MainController mainController) {
        this.mainController = mainController;
        this.larmanController = mainController.larmanController;
        allBundles = new ArrayList<>();
        group = new Group();
        scene = new SubScene(group, 1, 1, true, null);
        parent.getChildren().add(scene);
        this.scene.heightProperty().bind(parent.heightProperty());
        this.scene.widthProperty().bind(parent.widthProperty());
        scene.setFill(Color.valueOf("#37474f"));

        camera = new PerspectiveCamera(true);
        scene.setCamera(camera);
        camera.translateXProperty().set(0);
        camera.translateYProperty().set(0);
        camera.translateZProperty().set(-10);
        camera.setNearClip(1);
        camera.setFarClip(1000);
        initControl(group, scene);
    }

    private void initControl(Group group, SubScene scene) {
        Rotate xRotate;
        Rotate yRotate;
        group.getTransforms().addAll(
                xRotate = new Rotate(0, Rotate.X_AXIS),
                yRotate = new Rotate(0, Rotate.Y_AXIS)
        );
        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        /*scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W: {
                    transY.set(transY.get() + 5.0);
                    break;
                }
                case S: {
                    transY.set(transY.get() - 5.0);
                    break;
                }
                case A: {
                    transX.set(transX.get() - 5.0);
                    break;
                }
                case D: {
                    transX.set(transX.get() + 5.0);
                    break;
                }
            }
        });*/

        scene.setOnMousePressed(event -> {
            switch (event.getButton()) {
                case SECONDARY: {
                    anchorX = event.getSceneX();
                    anchorY = event.getSceneY();
                    anchorAngleX = angleX.get();
                    anchorAngleY = angleY.get();
                    break;
                }
                case PRIMARY: {
                    lastPoint = new Point2D(event.getSceneX(), event.getSceneY());
                    groupTranslate = new Point2D(group.getTranslateX(), group.getTranslateY());
                    break;
                }
            }

        });

        scene.setOnMouseDragged(event -> {
            switch (event.getButton()) {
                case SECONDARY: {
                    angleX.set(anchorAngleX - (anchorY - event.getSceneY()));
                    angleY.set(anchorAngleY + anchorX - event.getSceneX());
                    break;
                }
                case PRIMARY: {
                    Point2D direction = new Point2D((event.getSceneX() - lastPoint.getX())/50.0, (event.getSceneY() - lastPoint.getY())/50.0);
                    group.translateXProperty().set(groupTranslate.getX() + direction.getX());
                    group.translateYProperty().set(groupTranslate.getY() + direction.getY());
                    break;
                }
            }
        });

        scene.addEventHandler(ScrollEvent.SCROLL, event -> {
            double delta = event.getDeltaY();
            group.translateZProperty().set(group.getTranslateZ() + delta / 100.0d);
        });
    }


//https://github.com/afsalashyana/JavaFX-3D/blob/master/src/gc/tutorial/chapt4/Rotation3DWithMouse.java

    @Override
    public void draw() {
        for (BundleDto bundle : allBundles){
            Box box = new Box(bundle.width, bundle.height, bundle.length);
            box.setRotationAxis(new Point3D(0.0, 1.0, 0.0));
            box.setRotate(-bundle.angle);
            box.setTranslateX(bundle.getX() - initGroupTranslate.getX());
            box.setTranslateY(-bundle.getZ() + 1);
            box.setTranslateZ(bundle.getY() - initGroupTranslate.getY());

            PhongMaterial material = new PhongMaterial();
            material.setDiffuseColor(hex2Rgb(bundle.color));
            box.setMaterial(material);

            box.addEventHandler(MouseEvent.MOUSE_PRESSED, (event) -> {
                if (event.getButton() == MouseButton.PRIMARY) {

                }
            });
            group.getChildren().add(box);

        }
    }

    public void setFocusedBundle(BundleDto bundle) {
        clearBundles();
        larmanController.getAllCollidingBundles(allBundles,bundle);
        setInitialGroupTranslate();
        draw();
    }

    public void clearBundles() {
        allBundles.clear();
        group.getChildren().clear();
        draw();
    }

    private void setInitialGroupTranslate() {
        double minX = allBundles.get(0).position.getX();
        double maxX = allBundles.get(0).position.getX();
        double minY = allBundles.get(0).position.getY();
        double maxY = allBundles.get(0).position.getY();
        double maxZ = allBundles.get(0).z+allBundles.get(0).height;

        for (BundleDto bundle : allBundles) {
            BundlePresenter presenter = new BundlePresenter(bundle);
            for(helpers.Point2D position : presenter.getPoints()) {
                if (position.getX() < minX) {
                    minX = position.getX();
                }
                if (position.getX() > maxX) {
                    maxX = position.getX() ;
                }
                if ((bundle.height + bundle.z > maxZ)) {
                    maxZ = bundle.height + bundle.z;
                }
                if (position.getY() < minY) {
                    minY = position.getY();
                }
                if (position.getY() > maxY) {
                    maxY = position.getY() ;
                }
            }
        }
        double moyX = (maxX + minX)/2.0;
        double moyY = (maxY + minY)/2.0;
        double moyZ = maxZ/2.0;
        initGroupTranslate = new Point2D(moyX, moyY);
    }

}