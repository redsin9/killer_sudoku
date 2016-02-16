package dev.ken.red.controller;

import java.io.File;
import java.io.IOException;

import javafx.scene.Node;

/**
 * 
 * @author kenguyen
 *
 */
public interface ILevelGenerator {
	public Node getView();
	public void export(File file) throws IOException;
	public String validate();
}
