package models;

import textures.ModelTexture;

public class TexturedModel {
	
	private RawOBJModel rawModel;
	private ModelTexture texture;
	
	public TexturedModel(RawOBJModel model, ModelTexture texture){
		this.rawModel = model;
		this.texture = texture;
	}

	public RawOBJModel getRawOBJModel() {
		return rawModel;
	}

	public ModelTexture getTexture() {
		return texture;
	}

}
