package edu.hendrix.ev3.remote.net.topological;

import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Collection;

import edu.hendrix.ev3.ai.cluster.yuv.AdaptedYUYVImage;
import edu.hendrix.ev3.remote.net.RobotConstants;
import edu.hendrix.ev3.remote.net.TaggedMessage;
import edu.hendrix.ev3.util.StampedStorage;
import edu.hendrix.ev3.util.Util;

public class RobotMessage implements TaggedMessage {
	public final static int NUM_INITIAL_BYTES = 5;
	public final static int PREAMBLE_BYTES = NUM_INITIAL_BYTES + Integer.BYTES;
	public final static int NUM_BYTES = PREAMBLE_BYTES + RobotConstants.IMAGE_BYTES;
	public final static int MAX_ARCHIVES = RobotConstants.IMAGE_BYTES / StampedStorage.DATE_TIME_BYTES;
	
	private byte tag;
	private boolean isInitialized = true, hasPhoto = false, atGoal = false, hasArchives = false;
	private int photoNumber;
	private byte[] photoBytes;
	private LocalDateTime[] archives;
	
	private RobotMessage(byte tag, boolean isInitialized, boolean atGoal, boolean hasPhoto, int photoNumber, byte[] photoBytes, LocalDateTime[] archives) {
		Util.assertArgument(archives.length <= MAX_ARCHIVES, String.format("%d: Too many archives", archives.length));
		this.hasArchives = archives.length > 0;
		Util.assertArgument(!(hasPhoto && hasArchives), "Can't send both photo and archives");
		this.tag = tag;
		this.isInitialized = isInitialized;
		this.hasPhoto = hasPhoto;
		this.atGoal = atGoal;
		this.photoNumber = this.hasArchives ? archives.length : photoNumber;
		this.photoBytes = photoBytes;
		this.archives = archives;
	}
	
	public RobotMessage(byte tag, boolean isInitialized, boolean atGoal) {
		this(tag, isInitialized, atGoal, false, 0, null, new LocalDateTime[]{});
	}
	
	public RobotMessage(byte tag, boolean isInitialized, boolean atGoal, int photoNumber, AdaptedYUYVImage img) {
		this(tag, isInitialized, atGoal, true, photoNumber, img.copyBytes(), new LocalDateTime[]{});
	}
	
	public RobotMessage(byte tag, boolean isInitialized, LocalDateTime... archives) {
		this(tag, isInitialized, false, false, archives.length, null, archives);
	}
	
	public RobotMessage(byte tag, boolean isInitialized, Collection<LocalDateTime> archives) {
		this(tag, isInitialized, false, false, archives.size(), null, dumpInto(archives));
	}
	
	public static LocalDateTime[] dumpInto(Collection<LocalDateTime> archives) {
		LocalDateTime[] result = new LocalDateTime[archives.size()];
		int i = 0;
		for (LocalDateTime archive: archives) {
			result[i++] = archive;
		}
		return result;
	}
	
	public RobotMessage(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		this.tag = buffer.get();
		this.isInitialized = Util.byte2bool(buffer.get());
		this.atGoal = Util.byte2bool(buffer.get());
		this.hasPhoto = Util.byte2bool(buffer.get());
		this.hasArchives = Util.byte2bool(buffer.get());
		this.photoNumber = buffer.getInt();
		if (this.hasPhoto) {
			this.photoBytes = new byte[RobotConstants.IMAGE_BYTES];
			buffer.get(this.photoBytes);
		} else if (this.hasArchives) {
			archives = new LocalDateTime[this.photoNumber];
			byte[] archiveBytes = new byte[StampedStorage.DATE_TIME_BYTES];
			for (int i = 0; i < archives.length; i++) {
				buffer.get(archiveBytes);
				archives[i] = StampedStorage.bytes2LocalDateTime(archiveBytes);
			}
		}
	}
	
	public byte[] toBytes() {
		ByteBuffer buffer = ByteBuffer.allocate(NUM_BYTES);
		buffer.put(this.tag);
		buffer.put(Util.bool2byte(isInitialized));
		buffer.put(Util.bool2byte(atGoal));
		buffer.put(Util.bool2byte(hasPhoto));
		buffer.put(Util.bool2byte(hasArchives));
		
		if (hasPhoto) {
			buffer.putInt(photoNumber);
			buffer.put(photoBytes);
		} else if (hasArchives) {
			buffer.putInt(archives.length);
			for (int i = 0; i < archives.length; i++) {
				buffer.put(StampedStorage.localDateTime2bytes(archives[i]));
			}
		}
		return buffer.array();
	}
	
	public byte getTag() {
		return tag;
	}
	
	public boolean isInitialized() {return isInitialized;}
	
	public boolean atGoal() {return atGoal;}
	
	public boolean hasPhoto() {
		return hasPhoto;
	}
	
	public boolean hasArchives() {
		return hasArchives;
	}
	
	public int numArchives() {
		return archives.length;
	}
	
	public LocalDateTime getArchive(int i) {
		return archives[i];
	}
	
	public int getPhotoNumber() {
		return photoNumber;
	}
	
	public AdaptedYUYVImage getPhoto() {
		return new AdaptedYUYVImage(photoBytes, RobotConstants.WIDTH, RobotConstants.HEIGHT);
	}
}
